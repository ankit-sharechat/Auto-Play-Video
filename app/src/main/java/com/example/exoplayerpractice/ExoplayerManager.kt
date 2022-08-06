package com.example.exoplayerpractice

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object ExoplayerManager : Player.Listener {
    private val MAX_BUFFER_MS = 4000
    private var player: ExoPlayer? = null
    private var itemIndex: Int = -1
    private var defaultMediaSourceFactory: DefaultMediaSourceFactory? = null

    fun getExoPlayerInstance(
        context: Context,
        itemIndex: Int,
        videoUrl: String
    ): ExoPlayer {
        this.itemIndex = itemIndex
        if (player == null) {
            defaultMediaSourceFactory = DefaultMediaSourceFactory(CacheDataSourceFactory(context))
            player = ExoPlayer.Builder(context)
                .setLoadControl(
                    PreviewLoadControl(maxBufferMs = MAX_BUFFER_MS)
                )
                .build()
                .also { exoPlayer ->
                    exoPlayer.playWhenReady = false
                    exoPlayer.addListener(this)
                    exoPlayer.volume = 0F
                }
        }
        val mediaSource = defaultMediaSourceFactory?.createMediaSource(MediaItem.fromUri(videoUrl))
        if (mediaSource != null) {
            player?.playWhenReady = false
            player?.setMediaSource(mediaSource)
            player?.prepare()
        }
        return player!!
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == STATE_READY) {
            onStarted()
            startReadingUpdates()
        }
    }

    private fun onStarted() {
        _playCompletionFlow.tryEmit(PlayStatus.STARTED(itemIndex))
    }

    private fun onEnded() {
        _playCompletionFlow.tryEmit(PlayStatus.ENDED(itemIndex))
    }

    private fun startReadingUpdates() {
        handler.postDelayed({ readPlayerUpdates() }, 300)
    }

    private fun readPlayerUpdates() {
        if ((player?.currentPosition ?: 0L) >= MAX_BUFFER_MS
            || player?.playbackState == STATE_ENDED
        ) {
            player?.playWhenReady = false
            onEnded()
        } else {
            startReadingUpdates()
        }
    }

    sealed class PlayStatus {
        data class STARTED(val itemIndex: Int) : PlayStatus()
        data class ENDED(val itemIndex: Int) : PlayStatus()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val _playCompletionFlow: MutableSharedFlow<PlayStatus> =
        MutableSharedFlow(1, 1, BufferOverflow.DROP_OLDEST)
    val playCompletionFlow: SharedFlow<PlayStatus>
        get() = _playCompletionFlow
}