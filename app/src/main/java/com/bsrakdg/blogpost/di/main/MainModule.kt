package com.bsrakdg.blogpost.di.main

import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AppDatabase
import com.bsrakdg.blogpost.persistence.BlogPostDao
import com.bsrakdg.blogpost.repository.main.*
import com.bsrakdg.blogpost.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
@Module
object MainModule {

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostMainService(retrofitBuilder: Retrofit.Builder): BlogPostMainService {
        return retrofitBuilder
            .build()
            .create(BlogPostMainService::class.java)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
        blogPostMainService: BlogPostMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepositoryImpl(
            blogPostMainService = blogPostMainService,
            accountPropertiesDao = accountPropertiesDao,
            sessionManager = sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
        blogPostMainService: BlogPostMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepositoryImpl(
            blogPostMainService = blogPostMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        blogPostMainService: BlogPostMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepositoryImpl(
            blogPostMainService = blogPostMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }
}