package com.bsrakdg.blogpost.ui.main.create_blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.Injectable
import com.bsrakdg.blogpost.ui.DataStateChangeListener
import com.bsrakdg.blogpost.ui.UICommunicationListener
import com.bsrakdg.blogpost.ui.main.MainDependencyProvider
import com.bsrakdg.blogpost.ui.main.create_blog.state.CREATE_BLOG_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogViewState
import com.bsrakdg.blogpost.ui.main.create_blog.viewmodel.CreateBlogViewModel

abstract class BaseCreateBlogFragment : Fragment(), Injectable {

    val TAG: String = "BaseCreateBlogFragment"

    lateinit var dependencyProvider: MainDependencyProvider

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener

    lateinit var viewModel: CreateBlogViewModel

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {
            outState.putParcelable(
                CREATE_BLOG_VIEW_STATE_BUNDLE_KEY,
                viewModel.viewState.value
            )
        }
        super.onSaveInstanceState(outState)
    }

    private fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getViewModelProviderFactory())
                .get(CreateBlogViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        //restore state after process death
        savedInstanceState?.let { inState ->
            (inState[CREATE_BLOG_VIEW_STATE_BUNDLE_KEY] as CreateBlogViewState?)?.let { createBlogViewState ->
                viewModel.setViewState(createBlogViewState)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.createBlogFragment, activity as AppCompatActivity)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }

        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement MainDependencyProvider")
        }
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }
}