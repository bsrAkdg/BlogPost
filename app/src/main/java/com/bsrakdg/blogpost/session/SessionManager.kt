package com.bsrakdg.blogpost.session

import android.app.Application
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

}