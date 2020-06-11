package com.bsrakdg.blogpost.repository.main

import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.JobManager
import com.bsrakdg.blogpost.session.SessionManager
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("CreateBlogRepository") {

    private val TAG: String = "CreateBlogRepository"
}