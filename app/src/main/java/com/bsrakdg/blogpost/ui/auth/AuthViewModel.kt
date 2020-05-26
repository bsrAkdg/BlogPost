package com.bsrakdg.blogpost.ui.auth

import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : ViewModel()