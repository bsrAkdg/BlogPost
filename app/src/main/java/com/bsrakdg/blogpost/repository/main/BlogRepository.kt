package com.bsrakdg.blogpost.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
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
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.Constants.Companion.PAGINATION_PAGE_SIZE
import com.bsrakdg.blogpost.utils.DataState
import com.bsrakdg.blogpost.utils.DateConvertUtils
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.bsrakdg.blogpost.utils.GenericApiResponse
import com.bsrakdg.blogpost.utils.Response
import com.bsrakdg.blogpost.utils.ResponseType
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@MainScope
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
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        Log.d(TAG, "authToken: $authToken, query: $query, filterAndOrder: $filterAndOrder, page: $page")

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

                        // check query is in progress and is exhausted
                        blogViewState.blogFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > blogViewState.blogFields.blogList.size) {
                            blogViewState.blogFields.isQueryExhausted = true
                        }

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
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap { listBlogPosts ->
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    blogFields = BlogViewState.BlogFields(
                                        blogList = listBlogPosts,
                                        isQueryInProgress = true
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

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {

                    Log.d(TAG, "handleApiSuccessResponse:${response.body.response}")

                    onCompleteJob(
                        dataState = DataState.data(
                            data = BlogViewState(
                                viewBlogFields = BlogViewState.ViewBlogFields(
                                    isAuthorOfBlog = response.body.response == RESPONSE_HAS_PERMISSION_TO_EDIT
                                )
                            ),
                            response = null
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostMainService.isAuthorOfBlogPost(
                    authorization = "Token ${authToken.token!!}",
                    slug = slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                // not applicable
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // not applicable
            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }

        }.asLiveData()
    }

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    updateLocalDb(blogPost)
                } else {
                    onCompleteJob(
                        dataState = DataState.error(
                            response = Response(
                                message = ERROR_UNKNOWN,
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostMainService.deleteBlogPost(
                    authorization = "Token ${authToken.token!!}",
                    slug = blogPost.slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                // not applicable
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        dataState = DataState.data(
                            data = null,
                            response = Response(
                                message = SUCCESS_BLOG_DELETED, // check on fragment update ui list
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }
        }.asLiveData()
    }

    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                val updatedBlogPost = BlogPost(
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

                updateLocalDb(updatedBlogPost)

                withContext(Main) {
                    onCompleteJob(
                        dataState = DataState.data(
                            data = BlogViewState(
                                viewBlogFields = BlogViewState.ViewBlogFields(
                                    blogPost = updatedBlogPost
                                )
                            ),
                            response = Response(
                                message = response.body.response,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogPostMainService.updateBlog(
                    authorization = "Token ${authToken.token!!}",
                    slug = slug,
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                // not applicable
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.updateBlogPost(
                        pk = blogPost.pk,
                        title = blogPost.title,
                        body = blogPost.body,
                        image = blogPost.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }

        }.asLiveData()
    }

    // for onSaveInstanceState
    fun restoreBlogListFromCache(
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        Log.d(TAG, "query: $query, filterAndOrder: $filterAndOrder, page: $page")

        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = false,
            shouldCancelIfNoInternet = false,
            shouldLoadFromCache = true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {

                    // finish by viewing db cache
                    result.addSource(loadFromCache()) { blogViewState ->

                        // check query is in progress and is exhausted
                        blogViewState.blogFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > blogViewState.blogFields.blogList.size) {
                            blogViewState.blogFields.isQueryExhausted = true
                        }

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
                // ignore
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                // ignore
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap { listBlogPosts ->
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    blogFields = BlogViewState.BlogFields(
                                        blogList = listBlogPosts,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                // ignore
            }

            override fun setJob(job: Job) {
                addJob("restoreBlogListFromCache", job)
            }

        }.asLiveData()
    }

}