package com.bsrakdg.blogpost.api.main

import com.bsrakdg.blogpost.api.GenericResponse
import com.bsrakdg.blogpost.api.main.network_responses.BlogCreateUpdateResponse
import com.bsrakdg.blogpost.api.main.network_responses.BlogListSearchResponse
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AccountProperties
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

@MainScope
interface BlogPostMainService {

    @GET("account/properties")
    suspend fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): AccountProperties

    @PUT("account/properties/update")
    @FormUrlEncoded
    suspend fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): GenericResponse

    @PUT("account/change_password/")
    @FormUrlEncoded
    suspend fun updatePassword(
        @Header("Authorization") authorization: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmPassword: String
    ): GenericResponse

    @GET("blog/list")
    suspend fun searchListBlogPosts(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): BlogListSearchResponse

    @GET("blog/{slug}/is_author")
    suspend fun isAuthorOfBlogPost(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String
    ): GenericResponse

    @DELETE("blog/{slug}/delete")
    suspend fun deleteBlogPost(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String
    ): GenericResponse

    @Multipart
    @PUT("blog/{slug}/update")
    suspend fun updateBlog(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): BlogCreateUpdateResponse

    @Multipart
    @POST("blog/create")
    suspend fun createBlog(
        @Header("Authorization") authorization: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): BlogCreateUpdateResponse
}