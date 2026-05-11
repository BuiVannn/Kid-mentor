package com.buivan.ptalk_child

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

class StreamingAudioPlayer(private val opusEngine: OpusEngine) {
    private var audioTrack: AudioTrack? = null
    private val lock = Any()

    fun start() {
        synchronized(lock) {
            if (audioTrack != null) return

            val minBufferSize = AudioTrack.getMinBufferSize(
                OpusAudioFormat.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(OpusAudioFormat.PCM_FRAME_BYTES * 4)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(OpusAudioFormat.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBufferSize)
                .build()
                .apply { play() }
        }
    }

    fun playPacket(packet: ByteArray) {
        val frames = AudioFrameProtocol.unpackFrames(packet)
        synchronized(lock) {
            val track = audioTrack ?: return
            frames.forEach { frame ->
                val pcm = opusEngine.decodeFrame(frame)
                if (pcm != null && pcm.isNotEmpty()) {
                    track.write(pcm, 0, pcm.size)
                } else {
                    Log.w("StreamingAudioPlayer", "Dropped undecodable Opus frame")
                }
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            audioTrack?.let {
                try {
                    it.pause()
                    it.flush()
                } catch (_: Exception) {
                }
                try {
                    it.release()
                } catch (_: Exception) {
                }
            }
            audioTrack = null
        }
    }
}
