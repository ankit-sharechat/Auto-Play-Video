package com.example.exoplayerpractice

import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

class CacheDataSourceFactory constructor(private val context: Context) : DataSource.Factory {
    private val userAgent = "TestApp"
    private val httpDataSource = DefaultHttpDataSource.Factory().also {
        it.setUserAgent(userAgent)
    }
    private val defaultDataSourceFactory =
        DefaultDataSource.Factory(context, httpDataSource)

    override fun createDataSource(): DataSource {
        val lruEvictor = LeastRecentlyUsedCacheEvictor(1000 * 1024 * 100)
        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(context)
        val simpleCache =
            SimpleCache(File(context.cacheDir, "media"), lruEvictor, databaseProvider)
        return CacheDataSource(
            simpleCache,
            defaultDataSourceFactory.createDataSource(),
        )
    }
}