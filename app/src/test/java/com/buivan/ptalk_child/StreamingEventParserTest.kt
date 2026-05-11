package com.buivan.ptalk_child

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamingEventParserTest {

    @Test
    fun `parse known protocol states`() {
        assertEquals(StreamingEvent.Listening, StreamingEventParser.parse("LISTENING"))
        assertEquals(StreamingEvent.Processing, StreamingEventParser.parse("PROCESSING"))
        assertEquals(StreamingEvent.Speaking, StreamingEventParser.parse("SPEAKING"))
        assertEquals(StreamingEvent.Idle, StreamingEventParser.parse("IDLE"))
    }

    @Test
    fun `parse two-digit emotion code`() {
        val event = StreamingEventParser.parse("10")
        assertTrue(event is StreamingEvent.Emotion)
        assertEquals("10", (event as StreamingEvent.Emotion).code)
    }

    @Test
    fun `parse unknown string returns UnknownText`() {
        val event = StreamingEventParser.parse("HELLO")
        assertTrue(event is StreamingEvent.UnknownText)
        assertEquals("HELLO", (event as StreamingEvent.UnknownText).text)
    }
}
