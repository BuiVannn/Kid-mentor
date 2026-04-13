package com.buivan.ptalk_child

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

// ── Data classes ──────────────────────────────────────────────────────────────

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("redis") val redis: String
)

data class ProfileRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("role") val role: String = "Friend",
    @SerializedName("hobbies") val hobbies: List<String> = emptyList(),
    @SerializedName("grade") val grade: String = "grade1"
)

// ── Retrofit interface ────────────────────────────────────────────────────────

interface PTalkKidsApi {

    @Multipart
    @POST("voice/process")
    suspend fun processVoice(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("voice/health")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("api/profile/")
    suspend fun updateProfile(@Body profile: ProfileRequest): Response<Any>
}

// ── ApiService class ──────────────────────────────────────────────────────────

class ApiService(private val context: Context) {

    companion object {
        private const val BASE_URL = "http://171.226.10.121:8000/"
        private const val TAG = "ApiService"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(PTalkKidsApi::class.java)

    // ── Callback interface ────────────────────────────────────────────────
    interface AudioResponseCallback {
        fun onSuccess(audioFile: File)
        fun onError(errorMessage: String)
    }

    // ── 1. Gửi audio, nhận WAV về ─────────────────────────────────────────
    suspend fun sendAudio(audioFile: File, callback: AudioResponseCallback) {
        try {
            Log.d(TAG, "Đang gửi: ${audioFile.name}, ${audioFile.length()} bytes")

            val requestFile = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)

            val response = withContext(Dispatchers.IO) {
                api.processVoice(body)
            }

            when {
                response.isSuccessful -> {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val outputFile = withContext(Dispatchers.IO) {
                            saveWavToCache(responseBody)
                        }
                        if (outputFile != null) {
                            Log.d(TAG, "Nhận WAV thành công: ${outputFile.length()} bytes")
                            callback.onSuccess(outputFile)
                        } else {
                            callback.onError("Không lưu được file audio trả về")
                        }
                    } else {
                        callback.onError("Server trả về dữ liệu rỗng")
                    }
                }
                response.code() == 400 -> callback.onError("Có lỗi xảy ra khi đọc file thu âm.")
                response.code() == 500 -> callback.onError("Hệ thống đang bận, bé thử lại sau nhé!")
                response.code() == 504 -> callback.onError("Mạng bị chậm hoặc chờ quá lâu, bé thử lại nhé!")
                else -> callback.onError("Lỗi không xác định: ${response.code()}")
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout: ${e.message}")
            callback.onError("Mạng bị chậm hoặc chờ quá lâu, bé thử lại nhé!")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi: ${e.message}")
            callback.onError("Không kết nối được server. Kiểm tra mạng nhé!")
        }
    }

    // ── 2. Health check trước khi cho ghi âm ─────────────────────────────
    suspend fun isServerHealthy(): Boolean {
        return try {
            val response = withContext(Dispatchers.IO) { api.healthCheck() }
            val healthy = response.isSuccessful && response.body()?.status == "healthy"
            Log.d(TAG, "Health check: ${if (healthy) "OK ✅" else "FAIL ❌"}")
            healthy
        } catch (e: Exception) {
            Log.e(TAG, "Health check thất bại: ${e.message}")
            false
        }
    }

    // ── 3. Gửi profile trẻ em để AI xưng hô đúng ─────────────────────────
    suspend fun syncProfile(profile: ProfileRequest) {
        try {
            val response = withContext(Dispatchers.IO) { api.updateProfile(profile) }
            if (response.isSuccessful) {
                Log.d(TAG, "Sync profile thành công")
            } else {
                Log.w(TAG, "Sync profile thất bại: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync profile lỗi: ${e.message}")
        }
    }

    // ── Helper: lưu WAV bytes ra file cache ──────────────────────────────
    private fun saveWavToCache(responseBody: ResponseBody): File? {
        return try {
            val outputFile = File(context.cacheDir, "response_ai.wav")
            FileOutputStream(outputFile).use { output ->
                responseBody.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Lưu WAV thất bại: ${e.message}")
            null
        }
    }
}

//package com.buivan.ptalk_child
//
//import android.util.Log
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.asRequestBody
//import java.io.File
//import java.util.concurrent.TimeUnit
//
//class ApiService {
//
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(10, TimeUnit.SECONDS)
//        .readTimeout(60, TimeUnit.SECONDS)   // pipeline STT+LLM+TTS cần thời gian
//        .writeTimeout(15, TimeUnit.SECONDS)
//        .build()
//
//    // ── Thay bằng IP server thật của bạn ──────────────────────────────────
//    // Nếu server chạy local trên máy tính cùng mạng WiFi:
//    //   → Dùng IP LAN, ví dụ: "http://192.168.1.100:8000"
//    // Nếu dùng ngrok hoặc server có domain:
//    //   → Dùng URL đầy đủ, ví dụ: "https://xxxx.ngrok.io"
//    companion object {
//        private const val BASE_URL = "http://192.168.1.100:8000"
//        private const val ENDPOINT = "$BASE_URL/chat-audio"
//        private const val TAG = "ApiService"
//    }
//
//    // Callback interface để trả kết quả về MainActivity
//    interface AudioResponseCallback {
//        fun onSuccess(audioBytes: ByteArray)
//        fun onError(errorMessage: String)
//    }
//
//    fun sendAudio(audioFile: File, callback: AudioResponseCallback) {
//        Log.d(TAG, "Đang gửi file: ${audioFile.name}, size: ${audioFile.length()} bytes")
//
//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart(
//                name = "audio",
//                filename = audioFile.name,
//                body = audioFile.asRequestBody("audio/m4a".toMediaType())
//            )
//            .build()
//
//        val request = Request.Builder()
//            .url(ENDPOINT)
//            .post(requestBody)
//            .build()
//
//        // Gọi bất đồng bộ, không block UI thread
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: java.io.IOException) {
//                Log.e(TAG, "Gửi thất bại: ${e.message}")
//                callback.onError("Không kết nối được server: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (!response.isSuccessful) {
//                    Log.e(TAG, "Server lỗi: ${response.code}")
//                    callback.onError("Server lỗi: ${response.code}")
//                    return
//                }
//
//                val audioBytes = response.body?.bytes()
//                if (audioBytes == null || audioBytes.isEmpty()) {
//                    Log.e(TAG, "Server trả về audio rỗng")
//                    callback.onError("Server trả về dữ liệu rỗng")
//                    return
//                }
//
//                Log.d(TAG, "Nhận audio thành công: ${audioBytes.size} bytes")
//                callback.onSuccess(audioBytes)
//            }
//        })
//    }
//}
