package com.bsrakdg.blogpost.ui.main.create_blog.state

import android.net.Uri

data class CreateBlogViewState(

    // CreateBlogFragments variables
    var blogFields: NewBlogFields = NewBlogFields()
) {
    data class NewBlogFields(
        var newBlogTitle: String? = null,
        var newBlogBody: String? = null,
        var newImageUrl: Uri? = null
    )
}