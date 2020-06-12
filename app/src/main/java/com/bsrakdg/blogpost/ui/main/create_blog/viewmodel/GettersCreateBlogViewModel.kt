package com.bsrakdg.blogpost.ui.main.create_blog.viewmodel

import android.net.Uri

fun CreateBlogViewModel.getNewImageUri(): Uri? {
    getCurrentViewStateOrNew().let { createBlogViewState ->
        createBlogViewState.blogFields.let { newBlogFields ->
            return newBlogFields.newImageUri
        }
    }
}