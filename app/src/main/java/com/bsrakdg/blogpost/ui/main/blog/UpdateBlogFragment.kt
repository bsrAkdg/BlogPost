package com.bsrakdg.blogpost.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.blog.state.BlogStateEvent.UpdatedBlogPostEvent
import com.bsrakdg.blogpost.ui.main.blog.state.BlogViewState
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.BlogViewModel
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.getUpdatedBlogUri
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.onBlogPostUpdateSuccess
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.setUpdatedBlogFields
import com.bsrakdg.blogpost.utils.Constants
import com.bsrakdg.blogpost.utils.DataState
import com.bsrakdg.blogpost.utils.ErrorHandling
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_MUST_SELECT_IMAGE
import com.bsrakdg.blogpost.utils.Response
import com.bumptech.glide.RequestManager
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_update_blog.*
import kotlinx.android.synthetic.main.fragment_view_blog.blog_body
import kotlinx.android.synthetic.main.fragment_view_blog.blog_image
import kotlinx.android.synthetic.main.fragment_view_blog.blog_title
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

@MainScope
class UpdateBlogFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseBlogFragment(R.layout.fragment_update_blog) {

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

        image_container.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { blogViewState ->
                        // if this is not null, the blog post was updated
                        blogViewState.viewBlogFields.blogPost?.let { blogPost ->
                            viewModel.onBlogPostUpdateSuccess(blogPost).let {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { blogViewState ->
            blogViewState.updateBlogFields.let { updatedBlogFields ->
                // display blog properties
                setBlogProperties(
                    updatedBlogFields.updatedBlogTitle,
                    updatedBlogFields.updatedBlogBody,
                    updatedBlogFields.updatedImageUri
                )
            }
        })
    }

    private fun setBlogProperties(
        updatedBlogTitle: String?,
        updatedBlogBody: String?,
        updatedImageUrl: Uri?
    ) {
        requestManager
            .load(updatedImageUrl)
            .into(blog_image)

        blog_title.text = updatedBlogTitle
        blog_body.text = updatedBlogBody
    }

    private fun saveChanges() {
        var multiPartBody: MultipartBody.Part? = null

        viewModel.getUpdatedBlogUri()?.let { uri ->
            uri.path?.let { filePath ->

                val imageFile = File(filePath)
                Log.d(TAG, "UpdateBlogFragment: imageFile : $imageFile")

                val requestBody = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
                )
                // name = field name in serializer
                // filename = name of the image file
                // requestBody = file with file type information
                multiPartBody = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    requestBody
                )
            }
        }

        multiPartBody?.let {
            viewModel.setStateEvent(
                UpdatedBlogPostEvent(
                    title = blog_title.text.toString(),
                    body = blog_body.text.toString(),
                    image = it
                )
            )

            stateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
            uri = null,
            title = blog_title.text.toString(),
            body = blog_body.text.toString()
        )
    }

    private fun showErrorDialog(errorMessage: String) {
        stateChangeListener.onDataStateChange(
            dataState = DataState(
                error = Event(
                    StateError(
                        Response(
                            message = errorMessage,
                            responseType = ResponseType.Dialog()
                        )
                    )
                ),
                loading = Loading(isLoading = false),
                data = Data(
                    data = Event.dataEvent(data = null),
                    response = null
                )
            )
        )
    }


    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, Constants.GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    } ?: showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP : CROP CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP : CROP CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri $resultUri")
                    viewModel.setUpdatedBlogFields(
                        title = blog_title.text.toString(),
                        body = blog_body.text.toString(),
                        uri = resultUri
                    )

                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
    }

}