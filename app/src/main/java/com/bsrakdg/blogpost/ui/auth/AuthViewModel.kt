package com.bsrakdg.blogpost.ui.auth

import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.repository.auth.AuthRepository

class AuthViewModel
constructor(
    val authRepository: AuthRepository
) : ViewModel()