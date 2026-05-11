package com.buivan.ptalk_child

import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioFrameProtocol {
    private const val PREFIX_BYTES = 2
    private const val MAX_OPUS_FRAME_BYTES = 4096

    fun packFrame(opusFrame: ByteArray): ByteArray {
        require(opusFrame.isNotEmpty()) { "Opus frame must not be empty" }
        require(opusFrame.size <= 0xFFFF) { "Opus frame is too large" }

        return ByteBuffer.allocate(PREFIX_BYTES + opusFrame.size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(opusFrame.size.toShort())
            .put(opusFrame)
            .array()
    }

    fun unpackFrames(packet: ByteArray): List<ByteArray> {
        val buffer = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
        val frames = mutableListOf<ByteArray>()

        while (buffer.remaining() >= PREFIX_BYTES) {
            val length = buffer.short.toInt() and 0xFFFF
            if (length <= 0 || length > MAX_OPUS_FRAME_BYTES || length > buffer.remaining()) {
                throw ProtocolException("Invalid Opus frame length: $length")
            }

            val frame = ByteArray(length)
            buffer.get(frame)
            frames += frame
        }

        if (buffer.remaining() != 0) {
            throw ProtocolException("Trailing bytes after Opus frames: ${buffer.remaining()}")
        }

        return frames
    }
}

class ProtocolException(message: String) : Exception(message)
