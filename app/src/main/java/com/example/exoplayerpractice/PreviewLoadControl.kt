package com.example.exoplayerpractice

import androidx.media3.exoplayer.DefaultLoadControl

open class PreviewLoadControl constructor(
    private var maxBufferMs: Int = DEFAULT_MAX_BUFFER_MS
) : DefaultLoadControl() {
    override fun shouldContinueLoading(
        playbackPositionUs: Long,
        bufferedDurationUs: Long,
        playbackSpeed: Float
    ): Boolean {
        return bufferedDurationUs <= maxBufferMs * 1000 && playbackPositionUs == 0L
    }

    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        return bufferedDurationUs >= maxBufferMs * 1000
    }
}