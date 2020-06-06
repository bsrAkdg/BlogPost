package com.bsrakdg.blogpost.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.*
import com.bsrakdg.blogpost.utils.ErrorHandling
import com.bsrakdg.blogpost.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*

class BlogFragment : BaseBlogFragment(),
    BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: BlogListAdapter

    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()

        if (savedInstanceState == null) {
            viewModel.loadFirstPage()
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let { // completed loadFirstPage
            resetUI()
        }
    }

    private fun resetUI() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
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

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Int.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }


        // case 1: ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView : (keyboard or arrow) executing search... $searchQuery")
                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }
            }
            true
        }

        // case 2: SEARCH BUTTON CLICKED (in toolbar)
        (searchView.findViewById(R.id.search_go_btn) as View).setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView : (button) executing search... $searchQuery")

            viewModel.setQuery(searchQuery).let {
                onBlogSearchOrFilter()
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }
}