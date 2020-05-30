package com.bsrakdg.blogpost.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.Response
import com.bsrakdg.blogpost.ui.ResponseType
import com.bsrakdg.blogpost.utils.*
import com.bsrakdg.blogpost.utils.Constants.Companion.NETWORK_TIMEOUT
import com.bsrakdg.blogpost.utils.Constants.Companion.TESTING_NETWORK_DELAY
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

@InternalCoroutinesApi
abstract class NetworkBoundResource<ResponseObject, ViewStateType>(
    isNetworkAvailable: Boolean
) {
    private val TAG: String = "NetworkBoundResource"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        this.setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if (isNetworkAvailable) {

            // API REQUEST
            coroutineScope.launch {
                // simulate a network delay for testing
                delay(TESTING_NETWORK_DELAY)

                withContext(Main) {
                    // make network call
                    val apiResponse = createCall()
                    result.addSource(apiResponse) { response ->
                        result.removeSource(apiResponse)

                        coroutineScope.launch {
                            handleNetworkCall(response)
                        }
                    }
                }
            }

            // START TIME OUT ON IO THREAD
            GlobalScope.launch(IO) {
                delay(NETWORK_TIMEOUT)

                if (!job.isCompleted) {
                    Log.e(TAG, "NetworkBoundResource : job network timeout")
                    // below code triggers invokeOnCompletion of job
                    job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
                }
            }

        } else {
            // show network error dialog
            onErrorReturn(
                UNABLE_TODO_OPERATION_WO_INTERNET,
                shouldUseDialog = true,
                shouldUseToast = false
            )
        }
    }

    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when (response) {
            is ApiSuccessResponse -> {
                // Customize by every repository
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(
                    response.errorMessage,
                    shouldUseDialog = true,
                    shouldUseToast = false)
            }
            is ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: empty response (HTTP 204)")
                onErrorReturn(
                    "HTTP 204. Returned nothing.",
                    shouldUseDialog = true,
                    shouldUseToast = false
                )
            }
        }
    }

    @InternalCoroutinesApi
    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: called...")
        job = Job()
        job.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.e(TAG, "NetworkBoundResource: Job has been cancelled.")
                        cause?.let {
                            onErrorReturn(it.message, false, true)
                        } ?: onErrorReturn(ERROR_UNKNOWN, false, true)
                    } else if (job.isCompleted) {
                        Log.e(TAG, "NetworkBoundResource: Job has been completed...")
                        // Do nothing. Should be handled already.
                    }
                }

            })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()

        if (msg == null) {
            msg = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false //
        }

        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }

        if (useDialog) {
            responseType = ResponseType.Dialog()
        }

        onCompleteJob(
            DataState.error(
                response = Response(
                    message = msg,
                    responseType = responseType
                )
            )
        )
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun setJob(job: Job) // this referenced by repository (cancelable actions)
}