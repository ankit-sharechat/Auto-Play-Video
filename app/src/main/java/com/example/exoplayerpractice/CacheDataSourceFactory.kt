package com.example.exoplayerpractice

import android.content.Context
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
    private val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSource)

    override fun createDataSource(): DataSource {
        return CacheDataSource(
            VideoCache.getInstance(context),
            defaultDataSourceFactory.createDataSource(),
        )
    }
}

object VideoCache {
    private var sDownloadCache: SimpleCache? = null
    fun getInstance(context: Context): SimpleCache {
        if (sDownloadCache == null) sDownloadCache = SimpleCache(
            File(context.cacheDir, "exoCache"),
            LeastRecentlyUsedCacheEvictor(1000 * 1024 * 100),
            ExoDatabaseProvider(context)
        )
        return sDownloadCache!!
    }
}