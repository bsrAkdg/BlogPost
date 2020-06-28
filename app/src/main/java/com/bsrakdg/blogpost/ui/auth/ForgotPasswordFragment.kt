package com.bsrakdg.blogpost.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.auth.AuthScope
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.DataStateChangeListener
import com.bsrakdg.blogpost.ui.Response
import com.bsrakdg.blogpost.ui.ResponseType
import com.bsrakdg.blogpost.utils.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AuthScope
class ForgotPasswordFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_forgot_password) {

    private val TAG = "ForgotPasswordFragment";

    val viewModel: AuthViewModel by viewModels { // new way initialize viewModel
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    lateinit var webView: WebView
    lateinit var stateChangeListener: DataStateChangeListener

    private val webInteractionCallback: WebAppInterface.OnWebInteractionCallback =
        object : WebAppInterface.OnWebInteractionCallback {
            override fun onSuccess(email: String) {
                Log.d(TAG, "webInteractionCallback: onSuccess : a reset link will be sent to $email")
                onPasswordResetLinkSent()
            }

            override fun onError(errorMessage: String) {
                Log.d(TAG, "webInteractionCallback: onError : $errorMessage")

                val dataState = DataState.error<Any>(
                    response = Response(
                        message = errorMessage,
                        responseType = ResponseType.Dialog()
                    )
                )

                stateChangeListener.onDataStateChange(
                    dataState = dataState
                )
            }

            override fun onLoading(isLoading: Boolean) {
                Log.d(TAG, "webInteractionCallback: onLoading $isLoading")
                GlobalScope.launch(Main) {
                    stateChangeListener.onDataStateChange(
                        dataState = DataState.loading(
                            isLoading = isLoading,
                            cachedData = null
                        )
                    )
                }
            }
        }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0f,
                0f,
                0f
            )
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)

        loadPasswordResetWebView()

        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView() {
        stateChangeListener.onDataStateChange(
            DataState.loading(
                isLoading = true,
                cachedData = null
            )
        )

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.onDataStateChange(
                    DataState.loading(
                        isLoading = false,
                        cachedData = null
                    )
                )
            }
        }

        webView.loadUrl(Constants.PASSWORD_FORGOT_URL)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            WebAppInterface(webInteractionCallback),
            "AndroidTextListener"
        )

    }

    class WebAppInterface
    constructor(
        private val callback: OnWebInteractionCallback
    ) {

        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email = email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage = errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading = isLoading)
        }

        interface OnWebInteractionCallback {
            fun onSuccess(email: String)
            fun onError(errorMessage: String)
            fun onLoading(isLoading: Boolean)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must be implement DataStateChangeListener")
        }
    }
}
