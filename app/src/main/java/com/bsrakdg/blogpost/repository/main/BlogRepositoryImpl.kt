package com.bsrakdg.blogpost.repository.main

import android.util.Log
import com.bsrakdg.blogpost.api.GenericResponse
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.api.main.network_responses.BlogCreateUpdateResponse
import com.bsrakdg.blogpost.api.main.network_responses.BlogListSearchResponse
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.persistence.returnOrderedBlogQuery
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.repository.buildError
import com.bsrakdg.blogpost.repository.safeApiCall
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.utils.*
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@FlowPreview
@MainScope
class BlogRepositoryImpl
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): BlogRepository
{

    private val TAG: String = "AppDebug"
    override fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            dispatcher = IO,
            stateEvent = stateEvent,
            apiCall = {
                blogPostMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            },
            cacheCall = {
                blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
            }
        ){
            override suspend fun updateCache(networkObject: BlogListSearchResponse) {
                val blogPostList = networkObject.toList()
                withContext(IO) {
                    for(blogPost in blogPostList){
                        try{
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting blog: ${blogPost}")
                                blogPostDao.insertOrReplace(blogPost)
                            }
                        }catch (e: Exception){
                            Log.e(TAG, "updateLocalDb: error updating cache data on blog post with slug: ${blogPost.slug}. " +
                                    "${e.message}")
                            // Could send an error report here or something but I don't think you should throw an error to the UI
                            // Since there could be many blog posts being inserted/updated.
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<BlogPost>): DataState<BlogViewState> {
                val viewState = BlogViewState(
                    blogFields = BlogViewState.BlogFields(
                        blogList = resultObj
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            blogPostMainService.isAuthorOfBlogPost(
                "Token ${authToken.token!!}",
                slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {
                    val viewState = BlogViewState(
                        viewBlogFields = BlogViewState.ViewBlogFields(
                            isAuthorOfBlog = false
                        )
                    )
                    return when {

                        resultObj.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT) -> {
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        resultObj.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT) -> {
                            viewState.viewBlogFields.isAuthorOfBlog = true
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        else -> {
                            buildError(
                                ERROR_UNKNOWN,
                                UIComponentType.None(),
                                stateEvent
                            )
                        }
                    }
                }
            }.getResult()
        )
    }

    override fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ) =  flow {
        val apiResult = safeApiCall(IO){
            blogPostMainService.deleteBlogPost(
                "Token ${authToken.token!!}",
                blogPost.slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {

                    if(resultObj.response == SUCCESS_BLOG_DELETED){
                        blogPostDao.deleteBlogPost(blogPost)
                        return DataState.data(
                            response = Response(
                                message = SUCCESS_BLOG_DELETED,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Success()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                    else{
                        return buildError(
                            ERROR_UNKNOWN,
                            UIComponentType.Dialog(),
                            stateEvent
                        )
                    }
                }
            }.getResult()
        )
    }

    override fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            blogPostMainService.updateBlog(
                "Token ${authToken.token!!}",
                slug,
                title,
                body,
                image
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<BlogViewState> {

                    val updatedBlogPost = resultObj.toBlogPost()

                    blogPostDao.updateBlogPost(
                        updatedBlogPost.pk,
                        updatedBlogPost.title,
                        updatedBlogPost.body,
                        updatedBlogPost.image
                    )

                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data =  BlogViewState(
                            viewBlogFields = BlogViewState.ViewBlogFields(
                                blogPost = updatedBlogPost
                            ),
                            updateBlogFields = BlogViewState.UpdateBlogFields(
                                updatedBlogTitle = updatedBlogPost.title,
                                updatedBlogBody = updatedBlogPost.body,
                                updatedImageUri = null
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }

}