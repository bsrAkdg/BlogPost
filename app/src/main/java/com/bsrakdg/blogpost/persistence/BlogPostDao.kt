package com.bsrakdg.blogpost.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.utils.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(blogPost: BlogPost): Long // Long means the row number inserted into database

    @Query("""SELECT * FROM blog_post
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        LIMIT (:page * :pageSize)
    """)
    fun getAllBlogPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<BlogPost>>
}