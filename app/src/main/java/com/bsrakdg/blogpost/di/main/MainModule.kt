package com.bsrakdg.blogpost.di.main

import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.repository.main.account.AccountRepository
import com.bsrakdg.blogpost.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideBlogPostMainService(retrofitBuilder: Retrofit.Builder): BlogPostMainService {
        return retrofitBuilder
            .build()
            .create(BlogPostMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        blogPostMainService: BlogPostMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            blogPostMainService,
            accountPropertiesDao,
            sessionManager
        )
    }

}