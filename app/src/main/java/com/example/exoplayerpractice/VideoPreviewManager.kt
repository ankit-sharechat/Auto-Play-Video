package com.example.exoplayerpractice

import android.util.Log
import android.util.Range
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.*

class VideoPreviewManager {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val videoPreviewQueue: Queue<Int> = LinkedList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var listener: VideoPreviewListener
    fun attachRecyclerView(recyclerView: RecyclerView, listener: VideoPreviewListener) {
        this.recyclerView = recyclerView
        this.listener = listener
        attachScrollListener()
        collectPlayCompletions()
    }

    private fun collectPlayCompletions() {
        scope.launch {
            ExoplayerManager.playCompletionFlow.collectLatest { playStatus ->
                handlePlayStatus(playStatus)
            }
        }
    }

    private suspend fun handlePlayStatus(playStatus: ExoplayerManager.PlayStatus) {
        when (playStatus) {
            is ExoplayerManager.PlayStatus.ENDED -> playNext()
            is ExoplayerManager.PlayStatus.STARTED -> {}
        }
    }

    private fun attachScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scope.launch {
                    if (newState == SCROLL_STATE_IDLE) {
                        startPreview()
                    } else if (newState == SCROLL_STATE_DRAGGING) {
                        //stop all players
                        stopPlayer()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scope.launch {
                    if (dx == 0 && dy == 0) {
                        startPreview()
                    }
                }
            }
        })
    }

    private fun getRangeOfVisibleChildren(): Range<Int> {
        var firstIndex = -1
        var lastIndex = -1
        (recyclerView.layoutManager as LinearLayoutManager)
            .apply {
                firstIndex = findFirstCompletelyVisibleItemPosition()
                lastIndex = findLastCompletelyVisibleItemPosition()
            }
        Log.d("VideoPreviewManagerDebug", "[$firstIndex,$lastIndex]")
        return Range(firstIndex, lastIndex)
    }

    private suspend fun startPreview() {
        preparePreviewQueue()
        playNext()
    }

    private suspend fun playNext() {
        Log.d("VideoPreviewManagerDebug", "Attempt to Play From Queue")
        videoPreviewQueue.poll()?.let { itemIndex ->
            Log.d("VideoPreviewManagerDebug", "Playing $itemIndex From Queue")
            withContext(Dispatchers.Main) {
                listener.play(itemIndex)
            }
        }
    }

    private fun clearQueue() {
        videoPreviewQueue.clear()
    }

    private fun preparePreviewQueue() {
        val range = getRangeOfVisibleChildren()
        clearQueue()
        if (validRange(range)) {
            for (index in range.lower..range.upper) {
                videoPreviewQueue.add(index)
            }
        }
    }

    private fun validRange(range: Range<Int>): Boolean {
        return range.lower != -1 && range.upper != -1
    }

    private suspend fun stopPlayer() {
        withContext(Dispatchers.Main) {
            Log.d("VideoPreviewManagerDebug", "Stopping Preview")
            listener.pauseAll()
        }
    }

    interface VideoPreviewListener {
        fun play(index: Int)
        fun pauseAll()
    }
}