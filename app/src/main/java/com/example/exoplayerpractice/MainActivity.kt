package com.example.exoplayerpractice

import android.R.attr.data
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.STATE_BUFFERING
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.util.EventLogger
import com.example.exoplayerpractice.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*


class MainActivity : AppCompatActivity(), Player.Listener {
    val videos = listOf(
        "https://cdn.videvo.net/videvo_files/video/premium/video0402/small_watermarked/906_906-0292_preview.webm",
        "https://assets.mixkit.co/videos/preview/mixkit-portrait-of-a-woman-in-a-pool-1259-large.mp4",
        "https://assets.mixkit.co/videos/preview/mixkit-womans-feet-splashing-in-the-pool-1261-large.mp4",
        "https://assets.mixkit.co/videos/preview/mixkit-tree-with-yellow-flowers-1173-large.mp4",
        "https://cdn-aks.sharechat.com/slowProcessed_hb_343449_3186360_1659178283111_sc_v_2000_720.mp4"
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

    private var player: ExoPlayer? = null
    var initial = 0L
    private fun initializePlayer() {
        initial = System.currentTimeMillis()
        player = ExoPlayer.Builder(this)
            .setLoadControl(PreviewLoadControl())
            .build()
            .also { exoPlayer ->
                viewBinding.playerView.player = exoPlayer
                viewBinding.playerView.useController = false
                exoPlayer.setMediaItem(MediaItem.fromUri(videos[0]))
                exoPlayer.playWhenReady = false
                exoPlayer?.volume = 0f
                exoPlayer.prepare()
            }

        player?.addListener(this)
        val e = EventLogger(null, "TestTag")
        player?.addAnalyticsListener(e)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        pollStats()
    }

    private fun pollStats() {
        viewBinding.root.postDelayed({
            readStats()
            pollStats()
        }, 100)
    }

    val bufferData = ArrayList<Pair<Long, Long>>()

    private fun readStats() {
        val totalBufferedDuration = player?.totalBufferedDuration
        val plaback = player?.currentPosition
        val duration = player?.duration
        viewBinding.contentDuration.text = stringForTime(duration!!)
        viewBinding.bufferingDuration.text = """
            Playback At: $plaback
            Total Buffered : $totalBufferedDuration 
        """.trimIndent()
    }

    private fun setBufferingPercent() {
        player?.totalBufferedDuration
    }

    private fun setDuration() {
        val realDurationMillis = player?.duration!!
        viewBinding.contentDuration.text = stringForTime(realDurationMillis)
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