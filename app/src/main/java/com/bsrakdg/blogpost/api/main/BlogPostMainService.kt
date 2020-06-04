package com.bsrakdg.blogpost.api.main

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.utils.GenericApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface BlogPostMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>
}