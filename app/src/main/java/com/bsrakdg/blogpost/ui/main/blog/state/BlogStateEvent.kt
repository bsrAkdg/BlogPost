package com.bsrakdg.blogpost.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    // for onSaveInstanceState
    class RestoreBlogListFromCache : BlogStateEvent()

    class CheckAuthorOfBlogPostEvent : BlogStateEvent()

    class BlogDeleteEvent : BlogStateEvent()

    data class UpdatedBlogPostEvent(
        var title: String,
        var body: String,
        val image: MultipartBody.Part?
    ) : BlogStateEvent()

    class None : BlogStateEvent()
}