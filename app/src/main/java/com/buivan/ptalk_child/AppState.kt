package com.buivan.ptalk_child

enum class AppState {
    IDLE,        // Chờ user, nút hold sẵn sàng
    RECORDING,   // Đang giữ nút, đang ghi âm
    UPLOADING,   // Đã nhả tay, đang gửi lên server
    PLAYING,     // Model đang nói, hiện nút Cancel
    ERROR        // Có lỗi xảy ra
}