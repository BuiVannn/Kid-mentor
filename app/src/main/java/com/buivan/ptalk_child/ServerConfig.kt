package com.buivan.ptalk_child

object ServerConfig {
    const val HTTP_BASE_URL = "http://171.226.10.121:8000/"
    const val WS_URL = "ws://171.226.10.121:8000/ws"
    val TRANSPORT_MODE = TransportMode.AUTO
    val OPUS_ENGINE_MODE = OpusEngineMode.AUTO
}

enum class TransportMode {
    AUTO,
    STREAMING_ONLY,
    LEGACY_HTTP_ONLY
}
