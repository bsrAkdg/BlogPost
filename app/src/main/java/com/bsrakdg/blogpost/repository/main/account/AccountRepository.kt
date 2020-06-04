package com.bsrakdg.blogpost.repository.main.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
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
                        accountProperties.pk,
                        accountProperties.email,
                        accountProperties.username
                    )
                }
            }

        }.asLiveData()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "AccountRepository: cancelActiveJobs")
    }
}