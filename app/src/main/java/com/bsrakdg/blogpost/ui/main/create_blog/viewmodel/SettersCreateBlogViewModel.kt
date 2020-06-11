package com.bsrakdg.blogpost.ui.main.create_blog.viewmodel

import android.net.Uri
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogViewState


fun CreateBlogViewModel.setNewBlogFields(
    title: String?,
    body: String?,
    uri: Uri?
) {
    val update = getCurrentViewStateOrNew()
    val newBlogFields = update.blogFields
    title?.let { newBlogFields.newBlogTitle = it }
    body?.let { newBlogFields.newBlogBody = it }
    uri?.let { newBlogFields.newImageUri = it }
    update.blogFields = newBlogFields
    setViewState(update)
}

fun CreateBlogViewModel.clearNewBlogFields() {
    val update = getCurrentViewStateOrNew()
    update.blogFields = CreateBlogViewState.NewBlogFields()
    setViewState(update)
}