package com.buivan.ptalk_child

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFrameProtocolTest {

    @Test
    fun `packFrame writes little-endian prefix`() {
        val opus = byteArrayOf(0x11, 0x22, 0x33)
        val packed = AudioFrameProtocol.packFrame(opus)

        assertEquals(5, packed.size)
        assertEquals(3, packed[0].toInt() and 0xFF)
        assertEquals(0, packed[1].toInt() and 0xFF)
        assertArrayEquals(opus, packed.copyOfRange(2, packed.size))
    }

    @Test
    fun `unpackFrames handles multiple frames in one packet`() {
        val first = AudioFrameProtocol.packFrame(byteArrayOf(0x01, 0x02))
        val second = AudioFrameProtocol.packFrame(byteArrayOf(0x03))
        val merged = first + second

        val frames = AudioFrameProtocol.unpackFrames(merged)
        assertEquals(2, frames.size)
        assertArrayEquals(byteArrayOf(0x01, 0x02), frames[0])
        assertArrayEquals(byteArrayOf(0x03), frames[1])
    }

    @Test
    fun `unpackFrames throws for invalid length`() {
        val invalid = byteArrayOf(
            0x05, 0x00,
            0x01, 0x02
        )

        val thrown = runCatching { AudioFrameProtocol.unpackFrames(invalid) }.exceptionOrNull()
        assertTrue(thrown is ProtocolException)
    }
}
