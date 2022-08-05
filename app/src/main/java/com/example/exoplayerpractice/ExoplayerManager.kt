package com.example.exoplayerpractice

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadHelper.createMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

object ExoplayerManager : Player.Listener {
    private val MAX_BUFFER_MS = 3000

    private var player: ExoPlayer? = null
    private var defaultMediaSourceFactory: DefaultMediaSourceFactory? = null
    fun getExoPlayerInstance(context: Context, videoUrl: String): ExoPlayer {
        if (player == null) {
            Log.d("ExoplayerManager","creating instance")
            defaultMediaSourceFactory = DefaultMediaSourceFactory(CacheDataSourceFactory(context))
            player = ExoPlayer.Builder(context)
                .setLoadControl(PreviewLoadControl(maxBufferMs = MAX_BUFFER_MS))
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
        if (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE) {
            restartPlayer()
        }
    }


    private fun restartPlayer() {
        player?.seekTo(0L)
        player?.playWhenReady = true
    }
}