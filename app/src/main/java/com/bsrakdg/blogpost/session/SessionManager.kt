package com.bsrakdg.blogpost.session

import android.app.Application
import com.bsrakdg.blogpost.persistence.AuthTokenDao

class SessionManager
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

}