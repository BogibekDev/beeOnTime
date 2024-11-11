package com.jamesmobiledev.beeontime.ui

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.LocationDropDownAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityLoginBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.isKeyboardOpen
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.AuthManager
import com.jamesmobiledev.beeontime.manager.UserManager
import com.jamesmobiledev.beeontime.ui.admin.AdminActivity
import com.jamesmobiledev.beeontime.ui.user.MainActivity
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.clearSharedPref
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserEmails
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserId
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserStatus
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveUserEmails
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveUserId
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveUserName
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveUserStatus
import com.jamesmobiledev.beeontime.utils.statusAdmin


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loading: LottieAnimationView
    private lateinit var adapter: LocationDropDownAdapter
    private val emails = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (getUserId(this).isNotBlank()) {
            if (getUserStatus(this) == statusAdmin) {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
                return
            } else {
                AuthManager.logOut()
                val emails = getUserEmails(this)
                clearSharedPref(this)
                emails.forEach { saveUserEmails(this, it) }
            }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        getEmails()
        setupEmailAutoCompleteView()
        setupBottomSafeArea()
        setupWindowSoftInput()
        binding.btnLogin.setOnClickListener {
            loginUser(
                binding.emailAutoCompleteTV.text.toString(),
                binding.passwordEditText.text.toString()
            )
        }

        binding.btnContactHr.setOnClickListener {

        }


    }

    private fun getEmails() {
        emails.clear()
        emails.addAll(getUserEmails(this))
    }

    private fun setupEmailAutoCompleteView() {
        adapter = LocationDropDownAdapter(this, R.layout.item_drop_down, emails) {
            binding.emailAutoCompleteTV.setText(emails[it], false)
            binding.emailAutoCompleteTV.dismissDropDown()
        }
        binding.emailAutoCompleteTV.setAdapter(adapter)

        binding.emailAutoCompleteTV.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v)
                v.clearFocus()
                true
            } else {
                false
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val currentFocusedView = currentFocus
            if (currentFocusedView is EditText) {
                val outRect = Rect()
                currentFocusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
                    imm?.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
                    currentFocusedView.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupWindowSoftInput() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val constraintLayout = findViewById<LinearLayout>(R.id.linearLayout)
            val params = constraintLayout.layoutParams as ConstraintLayout.LayoutParams

            if (isKeyboardOpen()) {
                params.matchConstraintPercentHeight = 0.72f
            } else {
                params.matchConstraintPercentHeight = 0.6f
            }
            constraintLayout.layoutParams = params
        }
    }

    private fun setupBottomSafeArea() {
        binding.unSafeArea.doOnLayout {
            val navigationBarHeight = it.getNavigationBarHeight()
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (navigationBarHeight != 0) {
                    height = navigationBarHeight
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            loading.show()
            AuthManager.signIn(
                email = email,
                password = password,
                onError = {
                    loading.hide()
                    err(it)
                },
                onSuccess = { saveUserData(it) }
            )
        } else {
            Toast.makeText(this, "Username and Password cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveUserData(uid: String?) {

        uid?.let {
            UserManager.getById(
                uid = it,
                onSuccess = { user ->
                    if (user == null) {
                        loading.hide()
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        return@getById

                    }

                    saveUserId(this, it)
                    saveUserStatus(this, user.status)
                    saveUserName(this, user.name ?: "")
                    saveUserEmails(this, user.email ?: "")

                    loading.hide()
                    if (user.status == statusAdmin) {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
            )
        }

    }
}
