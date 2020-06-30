package com.bsrakdg.blogpost.api.main.network_responses

import com.bsrakdg.blogpost.models.BlogPost
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BlogListSearchResponse(

    @SerializedName("results")
    @Expose
    var results: List<BlogSearchResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    fun toList(): List<BlogPost>{
        val blogPostList: ArrayList<BlogPost> = ArrayList()
        for(blogPostResponse in results){
            blogPostList.add(
                blogPostResponse.toBlogPost()
            )
        }
        return blogPostList
    }


    override fun toString(): String {
        return "BlogListSearchResponse(results=$results, detail='$detail')"
    }
}