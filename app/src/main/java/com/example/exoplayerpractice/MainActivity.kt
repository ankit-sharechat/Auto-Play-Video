package com.example.exoplayerpractice

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.util.EventLogger
import com.example.exoplayerpractice.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity(), Player.Listener {
    val placeholders = listOf(
        "https://cdn3.sharechat.com/cv-f6f9b4d_1659376835023_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-41df8b2_1659376859442_sc_new_compressed_thumb.jpeg",
        "https://cdn-cf.sharechat.com/cv-39577bd1_1655998320308_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-1a6601d0_1659377255660_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-c81cc8f_1659376675601_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-1b3fe69_1659662675616_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-15fb92aa_1659491457055_sc_new_compressed_thumb.jpeg",
        "https://cdn-tc.sharechat.com/cv-104d3f5_1653377446880_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-d5f8f92d-c5e5-44fe-b27f-a16b3c45c443-260652e0-deba-4174-a027-714a806ebf8a_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-1b4aad80_1659437619158_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-354662fd_1659283364025_sc_new_compressed_thumb.jpeg",
        "https://cdn3.sharechat.com/cv-17f57729_1659456441355_sc_new_compressed_thumb.jpeg"
    )
    val videos = listOf(
        "https://cdn3.sharechat.com/slowProcessed_hb_109336_f6f9b4d_1659376835023_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_27823_41df8b2_1659376859442_sc_v_350_480.mp4",
        "https://cdn-cf.sharechat.com/slowProcessed_hb_707057_39577bd1_1655998320308_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_972617_1a6601d0_1659377255660_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_924383_c81cc8f_1659376675601_sc_v_350_360.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_86436_1b3fe69_1659662675616_sc_v_350_420.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_284978_15fb92aa_1659491457055_sc_v_350_360.mp4",
        "https://cdn-tc.sharechat.com/slowProcessed_lb_453445_104d3f5_1653377446880_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_801687_d5f8f92d-c5e5-44fe-b27f-a16b3c45c44_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_985298_1b4aad80_1659437619158_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slowProcessed_hb_401483_354662fd_1659283364025_sc_v_350_480.mp4",
        "https://cdn3.sharechat.com/slow_processed_h265_9523632584_390068_17f57729_1659456441355_sc_350_480.mp4"
    )

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initializePlayer()
    }

    val MAX_BUFFER_MS = 3000
    private var player: ExoPlayer? = null
    var initial = 0L
    private fun initializePlayer() {
        initial = System.currentTimeMillis()
        player = ExoPlayer.Builder(this)
            .setLoadControl(PreviewLoadControl(maxBufferMs = MAX_BUFFER_MS))
            .build()
            .also { exoPlayer ->
                viewBinding.playerView.player = exoPlayer
                viewBinding.playerView.useController = false
                val mediaSource = DefaultMediaSourceFactory(CacheDataSourceFactory(this))
                    .createMediaSource(MediaItem.fromUri(videos[3]))
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.playWhenReady = true
                exoPlayer?.volume = 0F
                exoPlayer.prepare()
            }

        player?.addListener(this)
        val e = EventLogger(null, "TestTag")
        player?.addAnalyticsListener(e)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        Log.d("PlayerDebug", "$playbackState")
        pollStats()
    }

    private fun pollStats() {
        viewBinding.root.postDelayed({
            readStats()
            pollStats()
        }, 100)
    }

    private fun readStats() {
        val plaback = player?.currentPosition!!
        if (plaback >= MAX_BUFFER_MS) {
            restartPlayer()
            return
        }

        if (plaback > 0L
            && (player?.playbackState == STATE_BUFFERING
                    || player?.playbackState == STATE_IDLE)
        ) {
            restartPlayer()
        }

        val playBackDuration = player?.duration
        val position = player?.currentPosition
        val totalBuffered = player?.totalBufferedDuration

        viewBinding.contentDuration.text = "Played: ${stringForTime(position!!)}"
        viewBinding.bufferingDuration.text = "Buffered : ${stringForTime(totalBuffered!!)}"
    }

    private fun restartPlayer() {
        player?.seekTo(0L)
        player?.playWhenReady = true
    }

    private fun stringForTime(timeMs: Long): String? {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        //  videoDurationInSeconds = totalSeconds % 60;
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

}