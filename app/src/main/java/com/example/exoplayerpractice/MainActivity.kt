package com.example.exoplayerpractice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val videoPreviewManager = VideoPreviewManager()
        viewBinding.recyclerView.apply {
            this.adapter = videoListAdapter
            asHorizontalLinearLayout()
            withInterItemSpace(8)
            attachPreviewManager(videoPreviewManager)
        }
    }
}

fun RecyclerView.attachPreviewManager(videoPreviewManager: VideoPreviewManager) {
    videoPreviewManager.attachToRecyclerView(recyclerView = this, previewRepeatMode = VideoPreviewManager.PreviewRepeatMode.Restart)
}

fun RecyclerView.withInterItemSpace(spaceDp: Int) {
    addItemDecoration(RecyclerViewMargin(spaceDp))
}

fun RecyclerView.asHorizontalLinearLayout() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
}
