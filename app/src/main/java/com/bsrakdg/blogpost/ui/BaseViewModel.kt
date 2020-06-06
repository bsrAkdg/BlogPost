package com.bsrakdg.blogpost.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    val TAG: String = "BaseViewModel"

    // _fieldName is recommended
    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData() // event
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData() // data

    // for update UI
    val viewState: LiveData<ViewState>
        get() = _viewState

    // for receive response by fragments, activity
    val dataState: LiveData<DataState<ViewState>> =
        Transformations.switchMap(_stateEvent) { stateEvent ->

            stateEvent?.let {
                handleStateEvent(stateEvent)
            }
        }

    // This method calls for UI events, if state event changes, view model handle it (handleStateEvent)
    fun setStateEvent(event: StateEvent) {
        _stateEvent.value = event
    }

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value?.let {
            it
        } ?: initNewViewState()
        return value
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    abstract fun initNewViewState(): ViewState

    abstract fun handleStateEvent(stateEvent: StateEvent): LiveData<DataState<ViewState>>


}