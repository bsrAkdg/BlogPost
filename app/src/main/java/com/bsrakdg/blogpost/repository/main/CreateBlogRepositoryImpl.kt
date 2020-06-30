package com.bsrakdg.blogpost.repository.main

import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.api.main.network_responses.BlogCreateUpdateResponse
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.safeApiCall
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogViewState
import com.bsrakdg.blogpost.utils.*
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@FlowPreview
@MainScope
class CreateBlogRepositoryImpl
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : CreateBlogRepository {

    override fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            blogPostMainService.createBlog(
                "Token ${authToken.token!!}",
                title,
                body,
                image
            )
        }

        emit(
            object : ApiResponseHandler<CreateBlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<CreateBlogViewState> {

                    // If they don't have a paid membership account it will still return a 200
                    // Need to account for that
                    if (resultObj.response != RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER) {
                        val updatedBlogPost = resultObj.toBlogPost()
                        blogPostDao.insertOrReplace(updatedBlogPost)
                    }
                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}