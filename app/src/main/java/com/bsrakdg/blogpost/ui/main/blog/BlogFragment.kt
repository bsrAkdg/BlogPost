package com.bsrakdg.blogpost.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.*
import com.bsrakdg.blogpost.utils.ErrorHandling
import com.bsrakdg.blogpost.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*

class BlogFragment : BaseBlogFragment(), BlogListAdapter.Interaction {

    private lateinit var recyclerAdapter: BlogListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        initRecyclerView()
        subscribeObservers()

        if (savedInstanceState == null) {
            viewModel.loadFirstPage()
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "BlogFragment, ViewState: $viewState")
            if (viewState != null) {
                recyclerAdapter.submitList(
                    list = viewState.blogFields.blogList,
                    isQueryExhausted = viewState.blogFields.isQueryExhausted
                )
            }
        })
    }

    private fun handlePagination(dataState: DataState<BlogViewState>) {
        // handle incoming data from datastate
        dataState.data?.let { data ->
            data.data?.let { event ->
                event.getContentIfNotHandled()?.let { blogViewState ->
                    Log.d(TAG, "BlogFragment, dataState: $blogViewState")
                    viewModel.handleIncomingBlogListData(blogViewState)
                }

            }
        }

        // check for pagination end (no more result)
        // must do this b/c server will return ApiErrorResponse if page is not valid
        // -> Meaning there is no more data
        dataState.error?.let { event ->
            event.peekContent().response.message?.let { errorResponse ->
                if (ErrorHandling.isPaginationDone(errorResponse)) {
                    // handle the error message event so it does not play on ui
                    event.getContentIfNotHandled()

                    // set query exhausted to update Recyclcer with
                    // "No more results..." list item
                    viewModel.setQueryExhausted(true)
                }
            }

        }
    }

    private fun initRecyclerView() {
        blog_post_recyclerview.apply {

            // layout manager
            layoutManager = LinearLayoutManager(this@BlogFragment.context)

            // item decoration
            val topSpacingItemDecoration = TopSpacingItemDecoration(padding = 30)
            removeItemDecoration(topSpacingItemDecoration)
            addItemDecoration(topSpacingItemDecoration)

            // create adapter
            recyclerAdapter = BlogListAdapter(
                interaction = this@BlogFragment,
                requestManager = requestManager
            )

            // add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()

                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "BlogFragment : attempting to load next page")
                        viewModel.nextPageNumber()
                    }
                }
            })

            adapter = recyclerAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        blog_post_recyclerview.adapter = null // leak memory
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        Log.d(TAG, "onItemSelected : $position. item : $item")
        // update state event for show selected item
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }
}