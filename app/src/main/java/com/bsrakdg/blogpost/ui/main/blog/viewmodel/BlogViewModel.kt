package com.bsrakdg.blogpost.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.persistence.BlogQueryUtils
import com.bsrakdg.blogpost.repository.main.BlogRepositoryImpl
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.BaseViewModel
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.*
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.utils.DataState
import com.bsrakdg.blogpost.utils.Loading
import com.bsrakdg.blogpost.utils.PreferenceKeys.Companion.BLOG_FILTER
import com.bsrakdg.blogpost.utils.PreferenceKeys.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepositoryImpl,
    private val sharedPreferences: SharedPreferences, // blog filters
    private val editor: SharedPreferences.Editor

) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )

        setBlogOrder(
            sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_ASC
            )!!
        )
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when (stateEvent) {
            is BlogSearchEvent -> {
                clearLayoutManagerState()
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken = authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                } ?: AbsentLiveData.create()
            }

            is CheckAuthorOfBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.isAuthorOfBlogPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                } ?: AbsentLiveData.create()
            }

            is BlogDeleteEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                        authToken = authToken,
                        blogPost = getBlogPost()
                    )
                } ?: AbsentLiveData.create()
            }

            is UpdatedBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.updateBlogPost(
                        authToken = authToken,
                        slug = getSlug(),
                        title = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.title
                        ),
                        body = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.body
                        ),
                        image = stateEvent.image

                    )
                } ?: AbsentLiveData.create()
            }

            is RestoreBlogListFromCache -> {
                return blogRepository.restoreBlogListFromCache(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage()
                )
            }

            is None -> {
                return object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            error = null,
                            loading = Loading(false),
                            data = null
                        )
                    }
                }
            }
        }
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}