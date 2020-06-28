package com.bsrakdg.blogpost.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import com.bsrakdg.blogpost.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.*
import com.bsrakdg.blogpost.utils.ErrorHandling
import com.bsrakdg.blogpost.utils.TopSpacingItemDecoration
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_blog.*
import javax.inject.Inject

@MainScope
class BlogFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseBlogFragment(R.layout.fragment_blog),
    BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var recyclerAdapter: BlogListAdapter

    private lateinit var searchView: SearchView

    val viewModel : BlogViewModel by viewModels {
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
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromCache()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
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
                recyclerAdapter.apply {
                    preLoadGlideImages(
                        requestManager,
                        viewState.blogFields.blogList
                    )

                    Log.d(TAG, "#list items: ${viewState.blogFields.blogList.size}")

                    recyclerAdapter.submitList(
                        list = viewState.blogFields.blogList,
                        isQueryExhausted = viewState.blogFields.isQueryExhausted
                    )
                }
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

    override fun restoreListPosition() {
        // update recyclerview layout manager with using view state when process death
        // this is needed for auto scroll last position after process death
        // Step 1. When process death : onPause -> saveLayoutManagerState (layout manager saved into view state)
        // Step 2. Execute process death, then open app
        // Step 3. Firstly receives data from cache, then sends new list to adapter, (submitList)
        // when adapter execute submitlist, it handled restoreListPosition interface on adapter
        // Step 4. Then this func update recyclerview layout manager with saved layout manager state
        viewModel.viewState.value?.blogFields?.layoutManagerState?.let { lmState ->
            blog_post_recyclerview?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterOptions()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun showFilterOptions() {

        // step O : show dialog
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val view = dialog.getCustomView()

            // step 1 : highlight the previous filter options
            val filter = viewModel.getFilter()
            if (filter == BLOG_FILTER_DATE_UPDATED) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            val order = viewModel.getOrder()
            if (order == BLOG_ORDER_ASC) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }

            // step 2 : listen for new applied filters
            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog : applying filters...")

                val selectedFilter = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                )
                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )
                var filter = BLOG_FILTER_DATE_UPDATED

                if (selectedFilter.text.toString() == getString(R.string.filter_author)) {
                    filter = BLOG_FILTER_USERNAME
                }

                var order = ""
                if (selectedOrder.text.toString() == getString(R.string.filter_desc)) {
                    order = "-"
                }

                Log.d(TAG, "filter: $filter, order: $order")

                // step 3 : save to shared preferences
                viewModel.saveFilterOptions(filter, order).let {
                    // step 4 : set the filter and order in the view model
                    viewModel.setBlogFilter(filter)
                    viewModel.setBlogOrder(order)
                    onBlogSearchOrFilter()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog : canceling filters...")
                dialog.dismiss()

            }

            dialog.show()
        }
    }

    private fun saveLayoutManagerState() {
        blog_post_recyclerview?.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }
}