package com.example.exoplayerpractice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exoplayerpractice.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), Player.Listener {
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        setupRecyclerView()
    }

    private lateinit var videoListAdapter: VideoListAdapter
    private fun setupRecyclerView() {
        videoListAdapter = VideoListAdapter(placeholders, videos)
        viewBinding.recyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = videoListAdapter
            addItemDecoration(RecyclerViewMargin())
        }

        val videoPreview = VideoPreviewManager()
        videoPreview.attachRecyclerView(
            viewBinding.recyclerView,
            object : VideoPreviewManager.VideoPreviewListener {
                override fun play(index: Int) {
                    videoListAdapter.playPreviewAt(index)
                }

                override fun pauseAll() {
                    videoListAdapter.pauseAllPreviews()
                }
            })
    }
}