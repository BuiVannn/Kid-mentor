# Hướng dẫn Tích hợp PTalk V2 Streaming WebSocket cho Android App

Tài liệu này cung cấp đặc tả kỹ thuật để đội ngũ phát triển Android App chuyển đổi kiến trúc từ cơ chế HTTP POST cũ sang cơ chế **Streaming hai chiều bằng WebSocket & Opus Codec** (tương tự như cách thiết bị vật lý ESP32 đang hoạt động).

Kiến trúc mới mang lại độ trễ cực thấp (dưới 1s), hỗ trợ truyền và phát âm thanh liên tục (streaming), tự động ngắt câu và xử lý gián đoạn mạng.

---

## 1. Thông số Kỹ thuật Cốt lõi
- **Giao thức:** WebSocket (`ws://` hoặc `wss://`)
- **Endpoint:** `ws://<domain_hoặc_ip>:8000/ws`
- **Định dạng Audio (Thu & Phát):** **Opus Codec** (Raw Opus Frames, không đóng gói màng Ogg/WebM).
- **Tần số lấy mẫu (Sample Rate):** `48000 Hz` (48kHz).
- **Số kênh (Channels):** `1` (Mono).
- **Kích thước Frame:** Thường là `20ms` (tương đương `960 samples` mỗi frame).

---

## 2. Giao thức Giao tiếp (Communication Protocol)

Kết nối WebSocket gửi và nhận 2 loại dữ liệu: **Text Message** (Dùng để điều khiển luồng) và **Binary Message** (Chứa dữ liệu âm thanh Opus).

### 2.1. Từ App Gửi lên Server (Uplink)

Khi người dùng nhấn nút thu âm (hoặc có cơ chế bắt giọng nói riêng), App thực hiện tuần tự các bước sau:

1. Gửi Text Message: `"START"`
   - Báo cho Server biết bắt đầu luồng ghi âm.
2. Gửi liên tục các Binary Message (Opus Frames)
   - Thu âm từ `AudioRecord` (PCM 16-bit, 48kHz, Mono).
   - Cắt PCM thành các block `20ms` (tương đương 960 mẫu).
   - Mã hóa (Encode) PCM thành khung Opus.
   - **QUAN TRỌNG (Cấu trúc gói tin Binary):** Server PTalk yêu cầu cấu trúc **Length-Prefixed Frame** (chống dính gói tin). 
     - 2 Byte đầu tiên: Độ dài của khung Opus (kiểu Unsigned Short, Little Endian).
     - N Byte tiếp theo: Dữ liệu Opus đã nén.
     - *Ví dụ:* Nếu Opus mã hóa ra mảng 120 byte, gửi mảng `[120_LE, opus_bytes...]` tổng cộng 122 byte.
   - Đẩy trực tiếp mảng byte này qua hàm `webSocket.send(bytes)`. Bạn có thể gộp nhiều khung Opus vào 1 lượt gửi (`webSocket.send`), miễn là mỗi khung đều có 2-Byte Prefix đi kèm.
3. Gửi Text Message: `"END"`
   - Báo cho Server kết thúc việc nói. Server sẽ lập tức gọi AI xử lý.

### 2.2. Từ Server Gửi về App (Downlink)

Server sẽ đẩy về trạng thái hoặc âm thanh phát trực tiếp (Streaming). App cần lắng nghe hàm `onMessage` (Text và Byte).

**Các Text Message Server sẽ gửi:**
- `"LISTENING"`: Xác nhận server đang nhận luồng âm thanh từ App.
- `"PROCESSING"`: Xác nhận server đang phân tích giọng nói (STT) và gọi AI (LLM). App có thể hiển thị hiệu ứng "Đang suy nghĩ...".
- **Mã Cảm Xúc (2 ký tự số, ví dụ `"00"`, `"10"`, `"02"`)**: Gửi trước khi âm thanh bắt đầu phát. Dùng để App đổi UI/Mặt cười cho sinh động.
- `"SPEAKING"`: Báo hiệu ngay sau đây sẽ là luồng âm thanh Binary được gửi tới.
- `"IDLE"`: Báo hiệu câu trả lời đã kết thúc. App quay về trạng thái rảnh.

**Binary Message (Cấu trúc Length-Prefixed tương tự Uplink):**
- Server sẽ trả về các gói WebSocket Binary. Mỗi gói có thể chứa **1 hoặc nhiều** khung Opus được nối liền nhau.
- App phải Parse gói tin theo vòng lặp:
  1. Đọc 2 byte đầu tiên (Little Endian) để lấy `Length`.
  2. Đọc `Length` byte tiếp theo để lấy `opus_data`.
  3. Giải mã `opus_data` thành PCM 16-bit 48kHz.
  4. Đẩy PCM vào `AudioTrack` để phát ra loa.
  5. Tiếp tục lặp lại từ bước 1 nếu byte của gói tin vẫn còn dư.

---

## 3. Kiến nghị Thư viện cho Android

Việc xử lý Opus Codec là bắt buộc để đạt tốc độ và chất lượng như thiết bị vật lý. 

1. **WebSocket:** Dùng thư viện `OkHttp` (có hỗ trợ WebSocket rất ổn định).
2. **Audio Encode/Decode (Opus):**
   - Không dùng `MediaRecorder` mặc định vì nó thường đóng gói thành màng `m4a` hoặc `ogg` (Server PTalk chỉ nhận raw Opus frame).
   - **Cách 1:** Dùng các thư viện JNI wrapper cho libopus như `scorekeep/Opus-Android` hoặc biên dịch mã nguồn `libopus` qua NDK.
   - **Cách 2:** Sử dụng thư viện `Tritonus` hoặc các biến thể cung cấp JNA wrapper cho Opus.
3. **Phát âm thanh Streaming:**
   - Sử dụng `AudioTrack` ở chế độ `MODE_STREAM` để ghi (write) các mảng byte PCM vào bộ đệm âm thanh liên tục ngay khi vừa giải mã xong từ server trả về.

---

## 4. Mô phỏng Mã giả (Pseudocode Flow)

```kotlin
// 1. Kết nối WebSocket
val request = Request.Builder().url("ws://<ip>:8000/ws").build()
val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
    
    override fun onMessage(webSocket: WebSocket, text: String) {
        when(text) {
            "LISTENING" -> showUI(Listening)
            "PROCESSING" -> showUI(Thinking)
            "SPEAKING" -> showUI(Talking)
            "IDLE" -> showUI(Waiting)
            else -> {
                if (text.length == 2 && text.all { it.isDigit() }) {
                    updateEmotion(text) // VD: "10" (buồn), "00" (bình thường)
                }
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Nhận được block chứa 1 hoặc nhiều khung Opus từ Server
        val buffer = ByteBuffer.wrap(bytes.toByteArray()).order(ByteOrder.LITTLE_ENDIAN)
        
        while (buffer.remaining() >= 2) {
            // 1. Đọc 2 byte đầu lấy kích thước (Unsigned Short)
            val frameLength = buffer.short.toInt() and 0xFFFF
            if (frameLength == 0 || frameLength > buffer.remaining()) break
            
            // 2. Đọc Opus Data
            val opusData = ByteArray(frameLength)
            buffer.get(opusData)
            
            // 3. Giải mã và Phát
            val pcmData = opusDecoder.decode(opusData)
            audioTrack.write(pcmData, 0, pcmData.size) 
        }
    }
})

// 2. Khi người dùng bấm thu âm
webSocket.send("START")
audioRecord.startRecording()
while(isRecording) {
    val pcmBuffer = readFromMic() // Mảng 960 mẫu (20ms)
    val opusData = opusEncoder.encode(pcmBuffer)
    
    // Đóng gói Length-Prefixed: [2-Byte Length][Opus Data]
    val buffer = ByteBuffer.allocate(2 + opusData.size).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(opusData.size.toShort())
    buffer.put(opusData)
    
    webSocket.send(ByteString.of(*buffer.array()))
}
audioRecord.stop()
webSocket.send("END")
```

---

## 5. Lưu ý Quan trọng
- **Opus Reset State:** Server đã được lập trình để tự động reset state encode/decode. Về phía App, nếu có hàm `opusEncoder.resetState()` hoặc khởi tạo lại decoder khi nhận lệnh `"IDLE"`, nên thực hiện để đảm bảo âm thanh không có tạp âm gắt giữa các phiên.
- **Xử lý Mất kết nối:** Cần bao bọc logic Reconnect cho WebSocket vì đây là TCP duy trì liên tục. Nếu rớt mạng khi đang `PROCESSING`, hãy ngắt `AudioTrack` và báo lỗi trên UI. Mọi cơ chế giữ an toàn AI đứt đoạn đã được bao bọc trên Backend.
