package com.example.exoplayerpractice

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE

class VideoPreviewManager {
    private lateinit var recyclerView: RecyclerView
    private lateinit var listener: VideoPreviewListener

    fun attachRecyclerView(recyclerView: RecyclerView, listener: VideoPreviewListener) {
        this.recyclerView = recyclerView
        this.listener = listener
        attachScrollListener()
    }

    private fun attachScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    //play first visible item
                    playFirstVisibleItem()
                } else {
                    //stop all players
                    stopPlayer()
                }
            }
        })
    }

    private fun playFirstVisibleItem() {
        val firstCompleteVisibleItemIndex =
            (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (firstCompleteVisibleItemIndex > -1) {
            if (recyclerView.isAttachedToWindow) {
                listener.play(firstCompleteVisibleItemIndex)
            }
        }
    }

    private fun stopPlayer() {
        listener.pauseAll()
    }

    interface VideoPreviewListener {
        fun play(index: Int)
        fun pauseAll()
    }
}