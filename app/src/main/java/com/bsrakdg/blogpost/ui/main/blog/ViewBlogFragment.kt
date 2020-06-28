package com.bsrakdg.blogpost.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.ui.AreYouSureCallback
import com.bsrakdg.blogpost.ui.UIMessage
import com.bsrakdg.blogpost.ui.UIMessageType
import com.bsrakdg.blogpost.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.BlogDeleteEvent
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.CheckAuthorOfBlogPostEvent
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.*
import com.bsrakdg.blogpost.utils.DateConvertUtils
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_view_blog.*
import javax.inject.Inject

@MainScope
class ViewBlogFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseBlogFragment(R.layout.fragment_view_blog) {

    val viewModel: BlogViewModel by viewModels {
        viewModelFactory
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // You do not have to save large list into onSaveInstanceState,
        // You should save to query and execute it for getting the list
        val viewState = viewModel.viewState.value
        viewState?.blogFields?.blogList = ArrayList()

        // restore state after process death
        outState.putParcelable(
            BLOG_VIEW_STATE_BUNDLE_KEY,
            viewModel.viewState.value
        )
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()

        //restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { blogViewState ->
                viewModel.setViewState(blogViewState)
            }

        }
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppBar()

        delete_button.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfBlogPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost()) {
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setIsAuthorOfBlogPost(
                            viewState.viewBlogFields.isAuthorOfBlog
                        )
                    }

                    data.response?.peekContent()?.let { response ->
                        if (response.message.equals(SUCCESS_BLOG_DELETED)) {
                            viewModel.removeDeletedBlogPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { blogViewState ->
            blogViewState.viewBlogFields.blogPost?.let { blogPost ->
                setBlogProperties(blogPost)
            }

            if (blogViewState.viewBlogFields.isAuthorOfBlog) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    private fun checkIsAuthorOfBlogPost() {
        viewModel.setIsAuthorOfBlogPost(false) // reset
        viewModel.setStateEvent(CheckAuthorOfBlogPostEvent())
    }

    private fun setBlogProperties(blogPost: BlogPost) {
        requestManager
            .load(blogPost.image)
            .into(blog_image)

        blog_title.text = blogPost.title
        blog_author.text = blogPost.username
        blog_update_date.text = DateConvertUtils.convertLongToStringDate(
            longDate = blogPost.date_updated
        )
        blog_body.text = blogPost.body
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(
            event = BlogDeleteEvent()
        )
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {
                // ignore
            }
        }
        uiCommunicationListener.onUIMessageReceived(
            uiMessage = UIMessage(
                message = getString(R.string.are_you_sure_delete),
                uiMessageType = UIMessageType.AreYouSureDialog(
                    callback = callback
                )
            )
        )
    }

    private fun navUpdateBlogFragment() {
        try {
            // prep for next fragment
            viewModel.setUpdatedBlogFields(
                title = viewModel.getBlogPost().title,
                body = viewModel.getBlogPost().body,
                uri = viewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)

        } catch (e: Exception) {
            Log.e(TAG, "Exception : ${e.message}")
            // show an error (optional)
        }
    }
}