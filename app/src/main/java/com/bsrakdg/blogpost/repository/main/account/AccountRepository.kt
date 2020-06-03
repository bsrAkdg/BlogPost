package com.bsrakdg.blogpost.repository.main.account

import android.util.Log
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.session.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {
    private val TAG: String = "AccountRepository"
    private var repositoryJob: Job? = null

    fun cancelActiveJobs() {
        Log.d(TAG, "AccountRepository: cancelActiveJobs")
    }
}