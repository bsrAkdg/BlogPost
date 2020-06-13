package com.bsrakdg.blogpost.ui.main.blog.state

import android.net.Uri
import android.os.Parcelable
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import kotlinx.android.parcel.Parcelize

const val BLOG_VIEW_STATE_BUNDLE_KEY = "com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState"

@Parcelize
data class BlogViewState(
    // Blog fragment variables
    var blogFields: BlogFields = BlogFields(),

    // View blog fragment variables
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),

    // Update blog fragment variables
    var updateBlogFields: UpdateBlogFields = UpdateBlogFields()
) : Parcelable {

    @Parcelize
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList(),
        var searchQuery: String = "", // for search bar
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED, // data updated
        var order: String = BLOG_ORDER_ASC // ""
    ) : Parcelable

    @Parcelize
    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlog: Boolean = false
    ) : Parcelable

    @Parcelize
    data class UpdateBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedImageUri: Uri? = null
    ) : Parcelable
}