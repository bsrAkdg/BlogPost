package com.bsrakdg.blogpost.ui.main.create_blog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bsrakdg.blogpost.repository.main.CreateBlogRepository
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.BaseViewModel
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.Loading
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogStateEvent
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogStateEvent.None
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogViewState
import com.bsrakdg.blogpost.utils.AbsentLiveData
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(stateEvent: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>> {
        when (stateEvent) {
            is CreateNewBlogEvent -> {
                return AbsentLiveData.create()
            }

            is None -> {
                // it the same with object: LiveData
                return liveData {
                    emit(
                        DataState(
                            error = null,
                            loading = Loading(isLoading = false),
                            data = null
                        )
                    )
                }
            }
        }
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}