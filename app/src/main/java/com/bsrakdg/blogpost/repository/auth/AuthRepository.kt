package com.bsrakdg.blogpost.repository.auth

import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.session.SessionManager

class AuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager
)