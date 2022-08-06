package com.example.exoplayerpractice

import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.exoplayerpractice.databinding.ItemVideoBinding

class VideoListAdapter(
    private val placeholders: List<String>,
    private val videosList: List<String>
) :
    RecyclerView.Adapter<VideoListAdapter.VideoItemItemViewHolder>(),
    VideoPreviewManager.VideoPreviewListener {

    class VideoItemItemViewHolder(private val itemBinding: ItemVideoBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private var videoUrl = ""
        fun bind(placeholder: String, videoUrl: String) {
            this.videoUrl = videoUrl
            itemBinding.placeholderView.load(placeholder)
            itemBinding.root.clipToOutline = true
            itemBinding.playerView.useController = false
        }

        fun pausePlayer() {
            itemBinding.playerView.player = null
            itemBinding.placeholderView.visibility = View.VISIBLE
            itemBinding.playerView.player?.playWhenReady = false
        }

        fun startPlay() {
            Log.d("VideoPreviewmanager","playing $videoUrl | $bindingAdapterPosition | $absoluteAdapterPosition | ${itemBinding.playerView}")
            itemBinding.placeholderView.visibility = View.INVISIBLE
            itemBinding.playerView.player =
                ExoplayerManager.getExoPlayerInstance(
                    context = itemBinding.root.context,
                    itemIndex = bindingAdapterPosition,
                    videoUrl = videoUrl
                )
            itemBinding.playerView.player?.playWhenReady = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemItemViewHolder {
        val itemBinding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context))
        return VideoItemItemViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: VideoItemItemViewHolder, position: Int) {
        holder.bind(placeholders[position], videosList[position])
    }

    override fun onBindViewHolder(
        holder: VideoItemItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()) {
            if (payloads[0] == PLAY) {
                holder.startPlay()
            } else if (payloads[0] == PAUSE) {
                holder.pausePlayer()
            }
        }
    }

    override fun getItemCount(): Int {
        return videosList.size
    }

    private val PLAY = 1
    private val PAUSE = 0
    var lastPlay = -1
    override fun play(index: Int) {
        if (lastPlay != -1) {
            notifyItemChanged(lastPlay, PAUSE)
        }
        notifyItemChanged(index, PLAY)
        lastPlay = index
    }

    override fun pauseAll() {
        lastPlay = -1
        notifyItemRangeChanged(0, videosList.size, PAUSE)
    }
}

class RecyclerViewMargin(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        //set right margin to all
        outRect.left = spaceDp * parent.context.resources.displayMetrics.density.toInt()
    }
}