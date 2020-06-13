package com.bsrakdg.blogpost.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.*
import com.bsrakdg.blogpost.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.bsrakdg.blogpost.ui.main.create_blog.viewmodel.clearNewBlogFields
import com.bsrakdg.blogpost.ui.main.create_blog.viewmodel.getNewImageUri
import com.bsrakdg.blogpost.ui.main.create_blog.viewmodel.setNewBlogFields
import com.bsrakdg.blogpost.utils.Constants.Companion.GALLERY_REQUEST_CODE
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_MUST_SELECT_IMAGE
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_SOMETHING_WRONG_WITH_IMAGE
import com.bsrakdg.blogpost.utils.SuccessHandling
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_blog.*
import kotlinx.android.synthetic.main.fragment_create_blog.blog_title
import kotlinx.android.synthetic.main.fragment_view_blog.blog_image
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class CreateBlogFragment : BaseCreateBlogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        blog_image.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        update_textview.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        subscribeObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.publish -> {
                val callback: AreYouSureCallback = object : AreYouSureCallback {
                    override fun proceed() {
                        publishNewBlog()
                    }

                    override fun cancel() {
                        // ignore
                    }

                }
                uiCommunicationListener.onUIMessageReceived(
                    UIMessage(
                        message = getString(R.string.are_you_sure_publish),
                        uiMessageType = UIMessageType.AreYouSureDialog(callback)
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)

                dataState.data?.let { data ->
                    data.response?.let { event ->
                        event.peekContent().message?.let { message ->
                            if (message == SuccessHandling.SUCCESS_BLOG_CREATED) {
                                viewModel.clearNewBlogFields()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.blogFields.let { newBlogFields ->
                setBlogProperties(
                    title = newBlogFields.newBlogTitle,
                    body = newBlogFields.newBlogBody,
                    image = newBlogFields.newImageUri
                )
            }
        })
    }

    private fun setBlogProperties(title: String?, body: String?, image: Uri?) {
        image?.let {
            dependencyProvider.getGlideRequestManager().load(image)
                .into(blog_image)
        } ?: setDefaultImage()

        blog_title.setText(title)
        blog_body.setText(body)
    }

    private fun setDefaultImage() {
        dependencyProvider.getGlideRequestManager()
            .load(R.drawable.default_image)
            .into(blog_image)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    } ?: showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP : CROP CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP : CROP CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri $resultUri")
                    viewModel.setNewBlogFields(
                        title = null,
                        body = null,
                        uri = resultUri
                    )

                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            title = blog_title.text.toString(),
            body = blog_body.text.toString(),
            uri = null
        )
    }

    private fun publishNewBlog() {
        var multiPartBody: MultipartBody.Part? = null

        viewModel.getNewImageUri()?.let { uri ->
            uri.path?.let { filePath ->

                val imageFile = File(filePath)
                Log.d(TAG, "CreateBlogFragment: imageFile : $imageFile")

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
                CreateNewBlogEvent(
                    title = blog_title.text.toString(),
                    body = blog_body.text.toString(),
                    image = it
                )
            )

            stateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)
    }
}