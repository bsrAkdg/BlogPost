package com.bsrakdg.blogpost.ui.main.blog.state

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    class CheckAuthorOfBlogPostEvent: BlogStateEvent()

    class BlogDeleteEvent : BlogStateEvent()

    class None : BlogStateEvent()
}