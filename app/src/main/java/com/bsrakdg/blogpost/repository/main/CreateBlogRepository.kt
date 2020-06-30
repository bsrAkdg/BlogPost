package com.bsrakdg.blogpost.repository.main

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.api.main.network_responses.BlogCreateUpdateResponse
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogViewState
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.DataState
import com.bsrakdg.blogpost.utils.DateConvertUtils
import com.bsrakdg.blogpost.utils.GenericApiResponse
import com.bsrakdg.blogpost.utils.Response
import com.bsrakdg.blogpost.utils.ResponseType
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@MainScope
class CreateBlogRepository
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("CreateBlogRepository") {

    private val TAG: String = "CreateBlogRepository"

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = true,
                shouldLoadFromCache = false
            ) {

            override suspend fun createCacheRequestAndReturn() {
                // not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                // If they don't have a paid membership account it will still return a 200
                // need an account for that
                if (response.body.response != RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER) {
                    val updateBlogPost = BlogPost(
                        pk = response.body.pk,
                        title = response.body.title,
                        slug = response.body.slug,
                        body = response.body.body,
                        image = response.body.image,
                        date_updated = DateConvertUtils.convertServerStringDateToLong(
                            sd = response.body.date_updated
                        ),
                        username = response.body.username
                    )
                    updateLocalDb(updateBlogPost)
                }

                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        dataState = DataState.data(
                            data = null,
                            response = Response(
                                message = response.body.response,
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogPostMainService.createBlog(
                    authorization = "Token ${authToken.token!!}",
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                // not applicable
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.insertOrReplace(blogPost)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }
}