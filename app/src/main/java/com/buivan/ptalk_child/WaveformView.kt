package com.buivan.ptalk_child

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class WaveLayer(
        val colorTop: Int,
        val colorBottom: Int,
        val alphaValue: Int,
        val frequency: Float,
        val speedMultiplier: Float,
        val baselineRatio: Float   // tỉ lệ chiều cao baseline (0f=top, 1f=bottom)
    )

    private val waveLayers = listOf(
        WaveLayer(0xFF5EC99A.toInt(), 0xFF3DAB7A.toInt(), 35, 1.0f, 0.6f, 0.55f),
        WaveLayer(0xFF7DD9B0.toInt(), 0xFF4DC990.toInt(), 25, 1.8f, 1.0f, 0.65f),
        WaveLayer(0xFFA8E8CC.toInt(), 0xFF6DCFAA.toInt(), 18, 0.7f, 0.4f, 0.48f)
    )

    private val paints = waveLayers.map {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    }

    private val path = Path()
    private var phase = 0f
    private var amplitude = 80f
    private var targetAmplitude = 80f

    private val animator = ValueAnimator.ofFloat(0f, (2f * Math.PI).toFloat()).apply {
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()
        addUpdateListener { anim ->
            phase = anim.animatedValue as Float
            amplitude += (targetAmplitude - amplitude) * 0.06f
            invalidate()
        }
    }

    init { animator.start() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        waveLayers.forEachIndexed { index, layer ->
            val baseline = h * layer.baselineRatio

            paints[index].shader = LinearGradient(
                0f, baseline - amplitude * 2f, 0f, baseline + amplitude,
                intArrayOf(
                    (layer.colorTop and 0x00FFFFFF) or (layer.alphaValue shl 24),
                    (layer.colorBottom and 0x00FFFFFF) or ((layer.alphaValue / 3) shl 24)
                ),
                null,
                Shader.TileMode.CLAMP
            )

            path.reset()

            // ── Viền TRÊN: đi từ trái sang phải ──────────────────────────────
            var x = 0f
            var firstPoint = true
            while (x <= w) {
                val y = baseline - amplitude * (0.5f + 0.5f * sin(
                    layer.frequency * (x / w) * 2f * Math.PI.toFloat()
                            + phase * layer.speedMultiplier
                ))
                if (firstPoint) { path.moveTo(x, y); firstPoint = false }
                else path.lineTo(x, y)
                x += 3f
            }

            // ── Viền DƯỚI: đi từ phải sang trái (ngược chiều) ────────────────
            // Dùng frequency và phase khác để 2 viền không giống nhau
            x = w
            while (x >= 0f) {
                val y = baseline + amplitude * 0.6f * (0.5f + 0.5f * sin(
                    layer.frequency * 1.3f * (x / w) * 2f * Math.PI.toFloat()
                            - phase * layer.speedMultiplier * 0.8f   // ngược phase
                ))
                path.lineTo(x, y)
                x -= 3f
            }

            path.close()
            canvas.drawPath(path, paints[index])
        }
    }


    // ── Gọi từ MainActivity theo state ───────────────────────────────────
    fun setStateIdle() { targetAmplitude = 70f; setSpeed(3500) }
    fun setStateRecording() { targetAmplitude = 120f; setSpeed(1800) }
    fun setStateUploading() { targetAmplitude = 90f; setSpeed(2500) }
    fun setStatePlaying() { targetAmplitude = 160f; setSpeed(900) }
    fun setStateError() { targetAmplitude = 30f; setSpeed(5000) }

    private fun setSpeed(ms: Long) { animator.duration = ms }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}

//package com.buivan.ptalk_child
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Paint
//import android.graphics.Path
//import android.util.AttributeSet
//import android.view.View
//import android.animation.ValueAnimator
//import android.view.animation.LinearInterpolator
//import kotlin.math.sin
//
//class WaveformView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null
//) : View(context, attrs) {
//
//    // ── Cấu hình 3 lớp sóng ──────────────────────────────────────────────
//    private data class WaveLayer(
//        val color: Int,
//        val alpha: Int,
//        val strokeWidth: Float,
//        val frequency: Float,    // tần số sóng
//        val speedMultiplier: Float,
//        val verticalOffset: Float // vị trí dọc (0f = giữa)
//    )
//
//    private val waveLayers = listOf(
//        WaveLayer(0xFF4CAF82.toInt(), 40, 8f,  1.2f, 1.0f,  0f),
//        WaveLayer(0xFF4CAF82.toInt(), 25, 5f,  2.0f, 1.5f, -20f),
//        WaveLayer(0xFF81C784.toInt(), 20, 4f,  0.8f, 0.7f,  20f)
//    )
//
//    private val paints = waveLayers.map { layer ->
//        Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = layer.color
//            alpha = layer.alpha
//            strokeWidth = layer.strokeWidth
//            style = Paint.Style.STROKE
//            strokeCap = Paint.Cap.ROUND
//        }
//    }
//
//    private val path = Path()
//    private var phase = 0f
//    private var amplitude = 60f      // biên độ sóng hiện tại
//    private var targetAmplitude = 60f
//
//    private val animator = ValueAnimator.ofFloat(0f, 2f * Math.PI.toFloat()).apply {
//        duration = 2000
//        repeatCount = ValueAnimator.INFINITE
//        repeatMode = ValueAnimator.RESTART
//        interpolator = LinearInterpolator()
//        addUpdateListener { anim ->
//            phase = anim.animatedValue as Float
//            // Smooth amplitude transition
//            amplitude += (targetAmplitude - amplitude) * 0.08f
//            invalidate()
//        }
//    }
//
//    init {
//        animator.start()
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        val centerY = height / 2f
//
//        waveLayers.forEachIndexed { index, layer ->
//            path.reset()
//            val yOffset = centerY + layer.verticalOffset
//            var firstPoint = true
//
//            var x = 0f
//            while (x <= width) {
//                val y = yOffset + amplitude *
//                        sin(layer.frequency * (x / width) * 2f * Math.PI.toFloat() +
//                                phase * layer.speedMultiplier)
//                if (firstPoint) {
//                    path.moveTo(x, y)
//                    firstPoint = false
//                } else {
//                    path.lineTo(x, y)
//                }
//                x += 4f
//            }
//            canvas.drawPath(path, paints[index])
//        }
//    }
//
//    // ── Gọi từ MainActivity theo state ───────────────────────────────────
//
//    fun setStateIdle() {
//        targetAmplitude = 30f
//        setAnimationSpeed(3000)
//    }
//
//    fun setStateRecording() {
//        targetAmplitude = 55f
//        setAnimationSpeed(1800)
//    }
//
//    fun setStateUploading() {
//        targetAmplitude = 40f
//        setAnimationSpeed(2500)
//    }
//
//    fun setStatePlaying() {
//        targetAmplitude = 90f
//        setAnimationSpeed(800)   // nhanh hơn → trông như đang nói
//    }
//
//    fun setStateError() {
//        targetAmplitude = 15f
//        setAnimationSpeed(4000)
//    }
//
//    private fun setAnimationSpeed(durationMs: Long) {
//        animator.duration = durationMs
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        animator.cancel()
//    }
//}
