package com.bsrakdg.blogpost.ui.auth

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.repository.auth.AuthRepository
import com.bsrakdg.blogpost.ui.BaseViewModel
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent.*
import com.bsrakdg.blogpost.ui.auth.state.AuthViewState
import com.bsrakdg.blogpost.ui.auth.state.LoginFields
import com.bsrakdg.blogpost.ui.auth.state.RegistrationFields
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        // if fragment change event from view model, base view model handle
        when (stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is RegisterAttemptEvent -> {
                return authRepository.attemptRegister(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }
            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
            is None -> {
                // for clear progress bar
                return object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState.data(
                            data = null,
                            response = null
                        )
                    }
                }
            }
        }
    }

    // this method triggers by register button onclick on RegisterFragment
    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        // do not continue if it has already been triggered
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    // this method triggers by login button onclick on LoginFragment
    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        // do not continue if it has already been triggered
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }

    fun cancelActiveJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}