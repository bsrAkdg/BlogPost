package com.bsrakdg.blogpost.di.main

import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AppDatabase
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.main.AccountRepository
import com.bsrakdg.blogpost.repository.main.BlogRepository
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
            blogPostMainService = blogPostMainService,
            accountPropertiesDao = accountPropertiesDao,
            sessionManager = sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        blogPostMainService: BlogPostMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(
            blogPostMainService = blogPostMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }
}