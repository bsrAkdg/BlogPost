package com.bsrakdg.blogpost.di.auth

import android.content.SharedPreferences
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.repository.auth.AuthRepository
import com.bsrakdg.blogpost.repository.auth.AuthRepositoryImpl
import com.bsrakdg.blogpost.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
@Module
object AuthModule {

    @JvmStatic
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): BlogPostAuthService {
        return retrofitBuilder
            .build()
            .create(BlogPostAuthService::class.java)
    }

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        blogPostAuthService: BlogPostAuthService,
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepositoryImpl(
            authTokenDao,
            accountPropertiesDao,
            blogPostAuthService,
            sessionManager,
            sharedPreferences,
            editor
        )
    }
}