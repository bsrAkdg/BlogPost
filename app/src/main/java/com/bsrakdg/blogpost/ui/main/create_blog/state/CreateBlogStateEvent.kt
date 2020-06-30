package com.bsrakdg.blogpost.ui.main.create_blog.state

import com.bsrakdg.blogpost.utils.StateEvent
import okhttp3.MultipartBody

sealed class CreateBlogStateEvent : StateEvent {

    data class CreateNewBlogEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part
    ) : CreateBlogStateEvent() {
        override fun errorInfo(): String {
            return "Unable to create a new blog post."
        }
    }

    class None : CreateBlogStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}