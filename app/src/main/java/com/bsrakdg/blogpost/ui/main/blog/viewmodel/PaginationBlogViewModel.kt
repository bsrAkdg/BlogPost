package com.bsrakdg.blogpost.ui.main.blog.viewmodel

import android.util.Log
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.BlogSearchEvent

fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
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