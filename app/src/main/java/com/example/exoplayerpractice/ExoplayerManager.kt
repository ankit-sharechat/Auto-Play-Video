package com.example.exoplayerpractice

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

object ExoplayerManager : Player.Listener {
    private val MAX_BUFFER_MS = 4000

    private var player: ExoPlayer? = null
    private var defaultMediaSourceFactory: DefaultMediaSourceFactory? = null
    fun getExoPlayerInstance(
        context: Context,
        videoUrl: String
    ): ExoPlayer {
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
        if (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
            restartPlayer()
        }
        if (playbackState == STATE_READY) {
            startReadingUpdates()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private fun startReadingUpdates() {
        handler.postDelayed({ readPlayerUpdates() }, 300)
    }

    private fun readPlayerUpdates() {
        val plabackPosition = player?.currentPosition ?: 0L
        if (plabackPosition >= MAX_BUFFER_MS) {
            restartPlayer()
        }
        startReadingUpdates()
    }


    private fun restartPlayer() {
        player?.seekTo(0L)
        player?.playWhenReady = true
    }
}