package com.bsrakdg.blogpost.ui.main.blog.viewmodel

import android.net.Uri
import com.bsrakdg.blogpost.models.BlogPost

fun BlogViewModel.onBlogPostUpdateSuccess(blogPost: BlogPost) {
    // update UpdateBlogFragment
    setUpdatedBlogFields(
        uri = null,
        title = blogPost.title,
        body = blogPost.body
    )
    setBlogPost(blogPost) // update ViewBlogFragment
    updateListItem(blogPost) // update BlogFragment
}

fun BlogViewModel.updateListItem(newBlogPost: BlogPost) {
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i].pk == newBlogPost.pk) {
            list[i] = newBlogPost
            break
        }
    }
    setBlogListData(list)
}

fun BlogViewModel.setUpdatedBlogFields(
    title : String?,
    body : String?,
    uri : Uri?
) {
    val update = getCurrentViewStateOrNew()
    val updatedBlogFields = update.updateBlogFields
    title?.let {
        updatedBlogFields.updatedBlogTitle = it
    }
    body?.let {
        updatedBlogFields.updatedBlogBody = it
    }
    uri?.let {
        updatedBlogFields.updatedImageUri = it
    }
    setViewState(update)
}

fun BlogViewModel.removeDeletedBlogPost() {
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i] == getBlogPost()) {
            list.remove(getBlogPost())
            break
        }
    }
    setBlogListData(list)
}

fun BlogViewModel.setQuery(query: String) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
}

fun BlogViewModel.setBlogListData(blogList: List<BlogPost>) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.blogList = blogList
    setViewState(update)
}

fun BlogViewModel.setBlogPost(blogPost: BlogPost) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost = blogPost
    setViewState(update)
}

fun BlogViewModel.setIsAuthorOfBlogPost(isAuthorOfBlog: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfBlog = isAuthorOfBlog
    setViewState(update)
}

fun BlogViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun BlogViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress = isInProgress
    setViewState(update)
}

fun BlogViewModel.setBlogFilter(filter: String?) {
    filter?.let {
        val update = getCurrentViewStateOrNew()
        update.blogFields.filter = filter
        setViewState(update)
    }
}

fun BlogViewModel.setBlogOrder(order: String) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.order = order
    setViewState(update)
}