package com.bsrakdg.blogpost.repository.main.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.bsrakdg.blogpost.api.GenericResponse
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.Response
import com.bsrakdg.blogpost.ui.ResponseType
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import com.bsrakdg.blogpost.utils.AbsentLiveData
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
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

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = false,
                shouldLoadFromCache = true
            ) {

            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    // finish by viewing the db cache
                    result.addSource(loadFromCache()) { accountViewState ->
                        onCompleteJob(
                            DataState.data(
                                data = accountViewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return blogPostMainService.getAccountProperties(
                    authorization = "Token ${authToken.token}"
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let { accountProperties ->
                    accountPropertiesDao.updateAccountProperties(
                        pk = accountProperties.pk,
                        email = accountProperties.email,
                        username = accountProperties.username
                    )
                }
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // Not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null) // do not care cache

                withContext(Main) {

                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                message = response.body.response,
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostMainService.saveAccountProperties(
                    authorization = "Token ${authToken.token!!}",
                    email = accountProperties.email,
                    username = accountProperties.username
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // ignore
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    pk = accountProperties.pk,
                    email = accountProperties.email,
                    username = accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // not applicable
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                withContext(Main) {

                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostMainService.updatePassword(
                    authorization = "Token ${authToken.token}",
                    oldPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmNewPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // ignore
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // ignore
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "AccountRepository: cancelActiveJobs")
    }
}