package com.bsrakdg.blogpost.ui.main.blog.viewmodel

import android.net.Uri
import com.bsrakdg.blogpost.models.BlogPost

fun BlogViewModel.getUpdatedBlogUri(): Uri? {
    getCurrentViewStateOrNew().let {
        it.updateBlogFields.updatedImageUri.let { uri ->
            return uri
        }
    }
}

fun BlogViewModel.getBlogPost(): BlogPost {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let { blogPost ->
            return blogPost
        } ?: getDummyBlogPost()
    }
}

fun BlogViewModel.getDummyBlogPost(): BlogPost {
    return BlogPost(
        pk = -1,
        title = "",
        slug = "",
        body = "",
        image = "",
        date_updated = 1,
        username = ""
    )
}

fun BlogViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let {
        it.viewBlogFields.blogPost?.let { blogPost ->
            return blogPost.slug
        }
    }
    return ""
}

fun BlogViewModel.isAuthorOfBlogPost(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.isAuthorOfBlog
    }
}

fun BlogViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.filter
    }
}

fun BlogViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.order
    }
}

fun BlogViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.searchQuery
    }
}

fun BlogViewModel.getPage(): Int {
    getCurrentViewStateOrNew().let {
        return it.blogFields.page
    }
}

fun BlogViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryExhausted
    }
}

fun BlogViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryInProgress
    }
}