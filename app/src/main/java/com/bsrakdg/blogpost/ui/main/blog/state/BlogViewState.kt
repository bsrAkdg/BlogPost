package com.bsrakdg.blogpost.ui.main.blog.state

import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED

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
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED, // data updated
        var order: String = BLOG_ORDER_ASC // ""
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlog: Boolean = false
    )
}