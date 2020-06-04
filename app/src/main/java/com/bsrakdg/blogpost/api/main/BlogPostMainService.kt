package com.bsrakdg.blogpost.api.main

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.api.GenericResponse
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.utils.GenericApiResponse
import retrofit2.http.*

interface BlogPostMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("account/properties/update")
    @FormUrlEncoded
    fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @PUT("account/change_password/")
    @FormUrlEncoded
    fun updatePassword(
        @Header("Authorization") authorization: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmPassword: String
    ): LiveData<GenericApiResponse<GenericResponse>>

}