package com.buivan.ptalk_child

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView

class CharacterAnimator(private val imageView: ImageView) {

    private var currentAnimator: AnimatorSet? = null

    // ─── IDLE: thở nhẹ nhàng ─────────────────────────────────────────────
    fun playIdle() {
        stopCurrent()
        imageView.setImageResource(R.drawable.char_idle)   // ← đổi ảnh

        val floatUp = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -18f).apply {
            duration = 1800
            interpolator = AccelerateDecelerateInterpolator()
        }
        val floatDown = ObjectAnimator.ofFloat(imageView, "translationY", -18f, 0f).apply {
            duration = 1800
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleXUp = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.04f).apply {
            duration = 1800; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleYUp = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.04f).apply {
            duration = 1800; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleXDown = ObjectAnimator.ofFloat(imageView, "scaleX", 1.04f, 1f).apply {
            duration = 1800; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleYDown = ObjectAnimator.ofFloat(imageView, "scaleY", 1.04f, 1f).apply {
            duration = 1800; interpolator = AccelerateDecelerateInterpolator()
        }

        val up = AnimatorSet().apply { playTogether(floatUp, scaleXUp, scaleYUp) }
        val down = AnimatorSet().apply { playTogether(floatDown, scaleXDown, scaleYDown) }

        currentAnimator = AnimatorSet().apply {
            playSequentially(up, down)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (currentAnimator != null) playIdle()
                }
            })
            start()
        }
    }

    // ─── RECORDING: rung nhẹ → đang lắng nghe ────────────────────────────
    fun playRecording() {
        stopCurrent()
        imageView.setImageResource(R.drawable.char_listening)  // ← đổi ảnh

        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.12f, 1f).apply {
            duration = 600; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.12f, 1f).apply {
            duration = 600; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val rotate = ObjectAnimator.ofFloat(imageView, "rotation", -3f, 3f, -3f).apply {
            duration = 600; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        currentAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, rotate)
            start()
        }
    }

    // ─── UPLOADING: xoay → đang suy nghĩ ─────────────────────────────────
//    fun playUploading() {
//        stopCurrent()
//        imageView.setImageResource(R.drawable.char_thinking)   // ← đổi ảnh
//
//        val rotate = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f).apply {
//            duration = 1000; repeatCount = ObjectAnimator.INFINITE
//        }
//        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.88f, 1f).apply {
//            duration = 1000; repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.88f, 1f).apply {
//            duration = 1000; repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//
//        currentAnimator = AnimatorSet().apply {
//            playTogether(rotate, scaleX, scaleY)
//            start()
//        }
//    }
    fun playUploading() {
        stopCurrent()
        imageView.setImageResource(R.drawable.char_thinking_3)
        imageView.rotation = 0f  // reset rotation từ state trước nếu có

        val scaleUp = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.08f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleUpY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.08f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleDown = ObjectAnimator.ofFloat(imageView, "scaleX", 1.08f, 1f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleDownY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.08f, 1f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }
        val translateUp = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -10f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }
        val translateDown = ObjectAnimator.ofFloat(imageView, "translationY", -10f, 0f).apply {
            duration = 500; interpolator = AccelerateDecelerateInterpolator()
        }

        val up = AnimatorSet().apply { playTogether(scaleUp, scaleUpY, translateUp) }
        val down = AnimatorSet().apply { playTogether(scaleDown, scaleDownY, translateDown) }

        currentAnimator = AnimatorSet().apply {
            playSequentially(up, down)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (currentAnimator != null) playUploading()
                }
            })
            start()
        }
    }


    // ─── PLAYING: nảy → đang nói ──────────────────────────────────────────
    fun playPlaying() {
        stopCurrent()
        imageView.setImageResource(R.drawable.char_talking_2)    // ← đổi ảnh

        // Reset rotation về 0 từ state trước (UPLOADING có xoay)
        imageView.rotation = 0f

        val bounceUp = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -22f).apply {
            duration = 300; interpolator = OvershootInterpolator(1.5f)
        }
        val bounceDown = ObjectAnimator.ofFloat(imageView, "translationY", -22f, 0f).apply {
            duration = 300; interpolator = OvershootInterpolator(1.5f)
        }
        val scaleXUp = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.08f).apply { duration = 300 }
        val scaleXDown = ObjectAnimator.ofFloat(imageView, "scaleX", 1.08f, 1f).apply { duration = 300 }
        val scaleYUp = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.95f).apply { duration = 300 }
        val scaleYDown = ObjectAnimator.ofFloat(imageView, "scaleY", 0.95f, 1f).apply { duration = 300 }

        val up = AnimatorSet().apply { playTogether(bounceUp, scaleXUp, scaleYUp) }
        val down = AnimatorSet().apply { playTogether(bounceDown, scaleXDown, scaleYDown) }

        currentAnimator = AnimatorSet().apply {
            playSequentially(up, down)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (currentAnimator != null) playPlaying()
                }
            })
            start()
        }
    }

    // ─── ERROR: lắc ngang → không hiểu ───────────────────────────────────
    fun playError() {
        stopCurrent()
        imageView.setImageResource(R.drawable.char_error)      // ← đổi ảnh

        val shake = ObjectAnimator.ofFloat(
            imageView, "translationX",
            0f, -20f, 20f, -16f, 16f, -10f, 10f, 0f
        ).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        currentAnimator = AnimatorSet().apply {
            play(shake)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    playIdle()  // tự về idle sau khi lắc xong
                }
            })
            start()
        }
    }

    // ─── Dọn dẹp ──────────────────────────────────────────────────────────
    fun stopCurrent() {
//        currentAnimator?.cancel()
//        currentAnimator = null
//        imageView.animate().cancel()
        val animatorToCancel = currentAnimator
        currentAnimator = null          // ← set null TRƯỚC
        animatorToCancel?.cancel()      // ← cancel SAU
        imageView.animate().cancel()
    }

    fun resetPosition() {
        imageView.apply {
            translationX = 0f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
            rotation = 0f
        }
    }
}

//package com.buivan.ptalk_child
//
//import android.animation.Animator
//import android.animation.AnimatorListenerAdapter
//import android.animation.AnimatorSet
//import android.animation.ObjectAnimator
//import android.view.animation.AccelerateDecelerateInterpolator
//import android.view.animation.OvershootInterpolator
//import android.widget.ImageView
//
//class CharacterAnimator(private val imageView: ImageView) {
//
//    private var currentAnimator: AnimatorSet? = null
//
//    // ─── IDLE: thở nhẹ nhàng lên xuống ───────────────────────────────────
//    fun playIdle() {
//        stopCurrent()
//
//        val floatUp = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -18f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val floatDown = ObjectAnimator.ofFloat(imageView, "translationY", -18f, 0f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleXUp = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.04f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleYUp = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.04f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleXDown = ObjectAnimator.ofFloat(imageView, "scaleX", 1.04f, 1f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleYDown = ObjectAnimator.ofFloat(imageView, "scaleY", 1.04f, 1f).apply {
//            duration = 1800
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//
//        val breatheUp = AnimatorSet().apply { playTogether(floatUp, scaleXUp, scaleYUp) }
//        val breatheDown = AnimatorSet().apply { playTogether(floatDown, scaleXDown, scaleYDown) }
//
//        currentAnimator = AnimatorSet().apply {
//            playSequentially(breatheUp, breatheDown)
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    // Lặp vô tận
//                    if (currentAnimator != null) playIdle()
//                }
//            })
//            start()
//        }
//    }
//
//    // ─── RECORDING: rung nhẹ + scale lớn hơn để thể hiện "đang lắng nghe" ─
//    fun playRecording() {
//        stopCurrent()
//
//        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.12f, 1f).apply {
//            duration = 600
//            repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.12f, 1f).apply {
//            duration = 600
//            repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val rotate = ObjectAnimator.ofFloat(imageView, "rotation", -3f, 3f, -3f).apply {
//            duration = 600
//            repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//
//        currentAnimator = AnimatorSet().apply {
//            playTogether(scaleX, scaleY, rotate)
//            start()
//        }
//    }
//
//    // ─── UPLOADING: xoay tròn liên tục như đang "suy nghĩ" ───────────────
//    fun playUploading() {
//        stopCurrent()
//
//        val rotate = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f).apply {
//            duration = 1000
//            repeatCount = ObjectAnimator.INFINITE
//        }
//        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.88f, 1f).apply {
//            duration = 1000
//            repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.88f, 1f).apply {
//            duration = 1000
//            repeatCount = ObjectAnimator.INFINITE
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//
//        currentAnimator = AnimatorSet().apply {
//            playTogether(rotate, scaleX, scaleY)
//            start()
//        }
//    }
//
//    // ─── PLAYING: nảy lên xuống như đang "nói chuyện" ────────────────────
//    fun playPlaying() {
//        stopCurrent()
//
//        // Reset rotation về 0 trước
//        imageView.rotation = 0f
//
//        val bounceUp = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -22f).apply {
//            duration = 300
//            interpolator = OvershootInterpolator(1.5f)
//        }
//        val bounceDown = ObjectAnimator.ofFloat(imageView, "translationY", -22f, 0f).apply {
//            duration = 300
//            interpolator = OvershootInterpolator(1.5f)
//        }
//        val scaleXUp = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.08f).apply {
//            duration = 300
//        }
//        val scaleXDown = ObjectAnimator.ofFloat(imageView, "scaleX", 1.08f, 1f).apply {
//            duration = 300
//        }
//        val scaleYUp = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.95f).apply {
//            duration = 300
//        }
//        val scaleYDown = ObjectAnimator.ofFloat(imageView, "scaleY", 0.95f, 1f).apply {
//            duration = 300
//        }
//
//        val up = AnimatorSet().apply { playTogether(bounceUp, scaleXUp, scaleYUp) }
//        val down = AnimatorSet().apply { playTogether(bounceDown, scaleXDown, scaleYDown) }
//
//        currentAnimator = AnimatorSet().apply {
//            playSequentially(up, down)
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    if (currentAnimator != null) playPlaying()
//                }
//            })
//            start()
//        }
//    }
//
//    // ─── ERROR: lắc ngang như "không hiểu" ───────────────────────────────
//    fun playError() {
//        stopCurrent()
//
//        val shake = ObjectAnimator.ofFloat(
//            imageView, "translationX",
//            0f, -20f, 20f, -16f, 16f, -10f, 10f, 0f
//        ).apply {
//            duration = 600
//            interpolator = AccelerateDecelerateInterpolator()
//        }
//
//        currentAnimator = AnimatorSet().apply {
//            play(shake)
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    // Sau khi lắc xong → về idle
//                    playIdle()
//                }
//            })
//            start()
//        }
//    }
//
//    // ─── Dừng animation hiện tại và reset ảnh ────────────────────────────
//    fun stopCurrent() {
//        currentAnimator?.cancel()
//        currentAnimator = null
//        imageView.animate().cancel()
//    }
//
//    fun resetPosition() {
//        imageView.apply {
//            translationX = 0f
//            translationY = 0f
//            scaleX = 1f
//            scaleY = 1f
//            rotation = 0f
//        }
//    }
//}
