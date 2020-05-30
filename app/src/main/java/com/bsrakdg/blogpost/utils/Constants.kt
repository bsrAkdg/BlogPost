package com.bsrakdg.blogpost.utils

class Constants {
    companion object {

        // urls
        const val BASE_URL = "https://open-api.xyz/api/"
        const val PASSWORD_FORGOT_URL = "https://open-api.xyz/password_reset/"

        // delays
        const val NETWORK_TIMEOUT = 3000L
        const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing

    }
}