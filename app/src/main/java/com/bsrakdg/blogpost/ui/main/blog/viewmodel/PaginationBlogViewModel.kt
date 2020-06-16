package com.bsrakdg.blogpost.ui.main.blog.viewmodel

import android.util.Log
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.BlogSearchEvent
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.RestoreBlogListFromCache
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState

fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.refreshFromCache() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreBlogListFromCache())
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
}

fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page =
        update.copy().blogFields.page // we do not want reference (use copy)
    update.blogFields.page = page + 1
    setViewState(update)
}

fun BlogViewModel.nextPageNumber() {
    if (!getIsQueryExhausted()
        && !getIsQueryInProgress()
    ) {

        Log.d(TAG, "BlogViewModel : attempting to load next page")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}

fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState) {
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)
}