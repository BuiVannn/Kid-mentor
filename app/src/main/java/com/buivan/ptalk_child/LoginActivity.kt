package com.buivan.ptalk_child

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.buivan.ptalk_child.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // ── Tài khoản giả (demo) ─────────────────────────────────────────────
    companion object {
        private const val DEMO_USERNAME = "ptalk"
        private const val DEMO_PASSWORD = "ptit2025"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Khi nhấn Done trên bàn phím → tự submit login
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }

        // Nút Đăng nhập chính
        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            attemptLogin()
        }

        // Nút Vào xem thử (guest)
        binding.btnGuest.setOnClickListener {
            hideKeyboard()
            goToMain(isGuest = true)
        }
    }

    // ── Xử lý đăng nhập ─────────────────────────────────────────────────
    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate không để trống
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.")
            shakeButton(binding.btnLogin)
            return
        }

        // Kiểm tra credentials giả
        if (username == DEMO_USERNAME && password == DEMO_PASSWORD) {
            hideError()
            animateSuccess()
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng.")
            shakeButton(binding.btnLogin)
        }
    }

    // ── Animation ────────────────────────────────────────────────────────
    private fun animateSuccess() {
        // Scale nút login → hiệu ứng "bấm được"
        binding.btnLogin.animate()
            .scaleX(0.96f).scaleY(0.96f)
            .setDuration(100)
            .withEndAction {
                binding.btnLogin.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(100)
                    .withEndAction { goToMain(isGuest = false) }
                    .start()
            }
            .start()
    }

    private fun shakeButton(view: View) {
        view.animate()
            .translationX(-12f).setDuration(60)
            .withEndAction {
                view.animate().translationX(12f).setDuration(60)
                    .withEndAction {
                        view.animate().translationX(-8f).setDuration(50)
                            .withEndAction {
                                view.animate().translationX(0f)
                                    .setDuration(50)
                                    .setInterpolator(AccelerateDecelerateInterpolator())
                                    .start()
                            }.start()
                    }.start()
            }.start()
    }

    // ── Navigation ───────────────────────────────────────────────────────
    private fun goToMain(isGuest: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("is_guest", isGuest)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private fun showError(message: String) {
        binding.tvLoginError.text = message
        binding.tvLoginError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvLoginError.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}
