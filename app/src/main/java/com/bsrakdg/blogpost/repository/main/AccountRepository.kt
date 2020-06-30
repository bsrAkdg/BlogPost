package com.bsrakdg.blogpost.repository.main

import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import com.bsrakdg.blogpost.utils.DataState
import com.bsrakdg.blogpost.utils.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@FlowPreview
@MainScope
interface AccountRepository {

    fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>
}