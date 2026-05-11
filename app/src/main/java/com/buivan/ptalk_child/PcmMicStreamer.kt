package com.buivan.ptalk_child

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import java.util.concurrent.atomic.AtomicBoolean

class PcmMicStreamer(
    private val opusEngine: OpusEngine,
    private val onOpusPacket: (ByteArray) -> Boolean,
    private val onError: (Throwable) -> Unit
) {
    private val isStreaming = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var streamThread: Thread? = null

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(): Boolean {
        if (!isStreaming.compareAndSet(false, true)) return true

        return try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                OpusAudioFormat.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) {
                throw IllegalStateException("Invalid AudioRecord buffer size: $minBufferSize")
            }

            val bufferSize = minBufferSize.coerceAtLeast(OpusAudioFormat.PCM_FRAME_BYTES * 4)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                OpusAudioFormat.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val recorder = audioRecord ?: throw IllegalStateException("AudioRecord init failed")
            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord is not initialized")
            }

            recorder.startRecording()
            streamThread = Thread({ streamLoop(recorder) }, "ptalk-pcm-mic-streamer").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
            true
        } catch (t: Throwable) {
            isStreaming.set(false)
            releaseRecorder()
            onError(t)
            false
        }
    }

    fun stop() {
        isStreaming.set(false)
        try {
            streamThread?.join(500)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        streamThread = null
        releaseRecorder()
    }

    private fun streamLoop(recorder: AudioRecord) {
        val pcmFrame = ByteArray(OpusAudioFormat.PCM_FRAME_BYTES)
        var offset = 0

        try {
            while (isStreaming.get()) {
                val read = recorder.read(pcmFrame, offset, pcmFrame.size - offset)
                if (read <= 0) continue
                offset += read

                if (offset == pcmFrame.size) {
                    val opusFrame = opusEngine.encodeFrame(pcmFrame.copyOf())
                    if (opusFrame != null && opusFrame.isNotEmpty()) {
                        val sent = onOpusPacket(AudioFrameProtocol.packFrame(opusFrame))
                        if (!sent) throw IllegalStateException("WebSocket rejected audio frame")
                    }
                    offset = 0
                }
            }
        } catch (t: Throwable) {
            if (isStreaming.get()) {
                onError(t)
            }
        }
    }

    private fun releaseRecorder() {
        audioRecord?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            try {
                it.release()
            } catch (_: Exception) {
            }
        }
        audioRecord = null
    }
}
