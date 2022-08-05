package com.example.exoplayerpractice

import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.TrackGroupArray
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.DefaultAllocator

open class PreviewLoadControl constructor(
    private var minBufferMs: Int = DEFAULT_MIN_BUFFER_MS,
    private var maxBufferMs: Int = DEFAULT_MAX_BUFFER_MS
) : LoadControl {

    companion object {
        /**
         * The default minimum duration of media that the player will attempt to ensure is buffered at all
         * times, in milliseconds.
         */
        const val DEFAULT_MIN_BUFFER_MS = 50000

        /**
         * The default maximum duration of media that the player will attempt to buffer, in milliseconds.
         */
        const val DEFAULT_MAX_BUFFER_MS = 50000

        /** A default size in bytes for a video buffer.  */
        const val DEFAULT_VIDEO_BUFFER_SIZE = 2000 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for an audio buffer.  */
        const val DEFAULT_AUDIO_BUFFER_SIZE = 200 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a text buffer.  */
        const val DEFAULT_TEXT_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a metadata buffer.  */
        const val DEFAULT_METADATA_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a camera motion buffer.  */
        const val DEFAULT_CAMERA_MOTION_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for an image buffer.  */
        const val DEFAULT_IMAGE_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE

        /** A default size in bytes for a muxed buffer (e.g. containing video, audio and text).  */
        val DEFAULT_MUXED_BUFFER_SIZE =
            DEFAULT_VIDEO_BUFFER_SIZE + DEFAULT_AUDIO_BUFFER_SIZE + DEFAULT_TEXT_BUFFER_SIZE

        /**
         * The buffer size in bytes that will be used as a minimum target buffer in all cases. This is
         * also the default target buffer before tracks are selected.
         */
        const val DEFAULT_MIN_BUFFER_SIZE = 200 * C.DEFAULT_BUFFER_SEGMENT_SIZE

    }

    private var allocator: DefaultAllocator? = null
    private var targetBufferBytesOverwrite = 0
    private var backBufferDurationUs: Long = 0
    private var retainBackBufferFromKeyframe = false
    private var targetBufferBytes = 0
    private var isLoading = false

    init {
        minBufferMs = DEFAULT_MIN_BUFFER_MS
        maxBufferMs = DEFAULT_MAX_BUFFER_MS
    }

    override fun onPrepared() {
        reset(false)
    }

    override fun onTracksSelected(
        renderers: Array<Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<ExoTrackSelection>
    ) {
        targetBufferBytes =
            if (targetBufferBytesOverwrite == C.LENGTH_UNSET) calculateTargetBufferBytes(
                renderers,
                trackSelections
            ) else targetBufferBytesOverwrite
        allocator!!.setTargetBufferSize(targetBufferBytes)
    }

    override fun onStopped() {
        reset(true)
    }

    override fun onReleased() {
        reset(true)
    }

    override fun getAllocator(): Allocator {
        return allocator!!
    }

    override fun getBackBufferDurationUs(): Long {
        return backBufferDurationUs
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return retainBackBufferFromKeyframe
    }

    override fun shouldContinueLoading(
        playbackPositionUs: Long, bufferedDurationUs: Long, playbackSpeed: Float
    ): Boolean {
        //Should only buffer until we are not buffered minBufferMs AND playback is not started.
        //We don`t want to buffer once the playback has started. Since we start playback once min buffer is reached,
        //it will never stuck in mid-playback
        return 1000 * minBufferMs >= bufferedDurationUs && playbackPositionUs == 0L
    }

    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        //Should only play after buffer ends
        return bufferedDurationUs >= minBufferMs * 1000
    }

    /**
     * Calculate target buffer size in bytes based on the selected tracks. The player will try not to
     * exceed this target buffer. Only used when `targetBufferBytes` is [C.LENGTH_UNSET].
     *
     * @param renderers The renderers for which the track were selected.
     * @param trackSelectionArray The selected tracks.
     * @return The target buffer size in bytes.
     */
    private fun calculateTargetBufferBytes(
        renderers: Array<Renderer>,
        trackSelectionArray: Array<ExoTrackSelection>
    ): Int {
        var targetBufferSize = 0
        for (i in renderers.indices) {
            targetBufferSize += getDefaultBufferSize(renderers[i].trackType)
        }
        return Math.max(DEFAULT_MIN_BUFFER_SIZE, targetBufferSize)
    }

    private fun reset(resetAllocator: Boolean) {
        targetBufferBytes =
            if (targetBufferBytesOverwrite == C.LENGTH_UNSET) DEFAULT_MIN_BUFFER_SIZE else targetBufferBytesOverwrite
        isLoading = false
        if (resetAllocator) {
            allocator!!.reset()
        }
    }

    private fun getDefaultBufferSize(trackType: @TrackType Int): Int {
        return when (trackType) {
            C.TRACK_TYPE_DEFAULT -> DEFAULT_MUXED_BUFFER_SIZE
            C.TRACK_TYPE_AUDIO -> DEFAULT_AUDIO_BUFFER_SIZE
            C.TRACK_TYPE_VIDEO -> DEFAULT_VIDEO_BUFFER_SIZE
            C.TRACK_TYPE_TEXT -> DEFAULT_TEXT_BUFFER_SIZE
            C.TRACK_TYPE_METADATA -> DEFAULT_METADATA_BUFFER_SIZE
            C.TRACK_TYPE_CAMERA_MOTION -> DEFAULT_CAMERA_MOTION_BUFFER_SIZE
            C.TRACK_TYPE_IMAGE -> DEFAULT_IMAGE_BUFFER_SIZE
            C.TRACK_TYPE_NONE -> 0
            C.TRACK_TYPE_UNKNOWN -> throw IllegalArgumentException()
            else -> throw IllegalArgumentException()
        }
    }

}