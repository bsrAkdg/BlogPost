package com.bsrakdg.blogpost.ui.main.blog.state

import com.bsrakdg.blogpost.models.BlogPost

data class BlogViewState(
    // Blog fragment variables
    var blogFields: BlogFields = BlogFields(),

    // View blog fragment variables
    var viewBlogFields: ViewBlogFields = ViewBlogFields()

    // Update blog fragment variables

) {
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList(),
        var searchQuery: String = "", // for search bar
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlog: Boolean = false
    )
}