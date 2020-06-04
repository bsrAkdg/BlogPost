package com.bsrakdg.blogpost.ui.main.account

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.repository.main.account.AccountRepository
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.BaseViewModel
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent.*
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import com.bsrakdg.blogpost.utils.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                } ?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    authToken.account_pk?.let { pk ->
                        accountRepository.saveAccountProperties(
                            authToken = authToken,
                            accountProperties = AccountProperties(
                                pk = pk,
                                email = stateEvent.email,
                                username = stateEvent.username
                            )
                        )
                    }
                } ?: AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken = authToken,
                        currentPassword = stateEvent.currentPassword,
                        newPassword = stateEvent.newPassword,
                        confirmNewPassword = stateEvent.confirmNewPassword
                    )
                } ?: AbsentLiveData.create()
            }

            is None -> {
                return AbsentLiveData.create()
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logout()
    }
}