package com.bsrakdg.blogpost.ui.main.blog.state

import com.bsrakdg.blogpost.utils.StateEvent
import okhttp3.MultipartBody

sealed class BlogStateEvent: StateEvent {

    class BlogSearchEvent : BlogStateEvent() {
        override fun errorInfo(): String {
            return "Error searching for blog posts."
        }
    }

    class RestoreBlogListFromCache: BlogStateEvent() {
        override fun errorInfo(): String {
            return "Unable to restore blog posts from cache."
        }
    }

    class CheckAuthorOfBlogPost: BlogStateEvent() {
        override fun errorInfo(): String {
            return "Error checking if you are the author of this blog post."
        }
    }

    class DeleteBlogPostEvent: BlogStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting that blog post."
        }
    }

    data class UpdateBlogPostEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part?
    ): BlogStateEvent() {
        override fun errorInfo(): String {
            return "Error updating that blog post."
        }
    }

    class None: BlogStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}