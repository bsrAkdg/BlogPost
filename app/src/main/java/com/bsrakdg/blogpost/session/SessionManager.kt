package com.bsrakdg.blogpost.session

import android.app.Application
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import javax.inject.Inject

class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

}