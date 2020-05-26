package com.bsrakdg.blogpost.di.auth

import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.repository.auth.AuthRepository
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.utils.Constants
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {

    @AuthScope
    @Provides
    fun provideFakeApiService(): BlogPostAuthService {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .build()
            .create(BlogPostAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        blogPostAuthService: BlogPostAuthService
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            blogPostAuthService,
            sessionManager
        )
    }
}