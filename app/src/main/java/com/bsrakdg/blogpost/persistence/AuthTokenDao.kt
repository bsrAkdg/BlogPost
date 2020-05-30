package com.bsrakdg.blogpost.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bsrakdg.blogpost.models.AuthToken

/** Authentication table queries */
@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAndReplace(authToken: AuthToken): Long

    @Query("UPDATE auth_token SET token = null WHERE account_pk = :pk")
    fun nullifyToken(pk: Int): Int

    @Query("SELECT * FROM auth_token WHERE account_pk = :pk")
    suspend fun searchByPk(pk: Int) : AuthToken?

}