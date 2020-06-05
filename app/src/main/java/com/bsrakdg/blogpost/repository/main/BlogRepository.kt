package com.bsrakdg.blogpost.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.api.main.network_responses.BlogListSearchResponse
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.JobManager
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.DateConvertUtils
import com.bsrakdg.blogpost.utils.GenericApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("BlogRepository") {

    private val TAG = "BlogRepository"

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = false,
            shouldLoadFromCache = true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {

                    // finish by viewing db cache
                    result.addSource(loadFromCache()) { blogViewState ->
                        onCompleteJob(
                            DataState.data(
                                data = blogViewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for (blogPostResponse in response.body.results) {
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateConvertUtils.convertServerStringDateToLong(
                                sd = blogPostResponse.date_updated
                            ),
                            username = blogPostResponse.username
                        )
                    )
                }
                updateLocalDb(blogPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return blogPostMainService.searchListBlogPosts(
                    authorization = "Token ${authToken.token!!}",
                    query = query
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.getAllBlogPosts()
                    .switchMap { listBlogPosts ->
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    blogFields = BlogViewState.BlogFields(
                                        blogList = listBlogPosts
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if (cacheObject != null) {
                    withContext(IO) {
                        for (blogPost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting blog: $blogPost")
                                    blogPostDao.insertOrReplace(blogPost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updatind cache on blog post with slug: ${blogPost.slug}"
                                )

                                // optional error handling?
                            }
                        }
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }
}