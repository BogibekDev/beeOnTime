package com.jamesmobiledev.beeontime.ui.admin.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.databinding.ActivityEditBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.UserManager
import com.jamesmobiledev.beeontime.model.User
import com.jamesmobiledev.beeontime.utils.isEdited

class EditUserActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditBinding.inflate(layoutInflater) }
    private lateinit var user: User
    private lateinit var loading: LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        getUser()
        binding.apply {
            nameEditText.setText(user.name)
            lastNameEditText.setText(user.lastName)
            usernameEditText.setText(user.email)
            passwordEditText.setText(user.password)

            btnLogin.setOnClickListener {
                val name = nameEditText.text.toString()
                val lastName = lastNameEditText.text.toString()
                val email = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                if (name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        this@EditUserActivity,
                        "name,email and password are not empty",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val newUser = user.copy(
                        name = name,
                        lastName = lastName,
                        email = email,
                        password = password
                    )
                    loading.show()
                    UserManager.update(
                        user = user,
                        newUser = newUser,
                        onError = {
                            loading.hide()
                            err(it)
                        },
                        onSuccess = {
                            Toast.makeText(this@EditUserActivity, "success", Toast.LENGTH_SHORT)
                                .show()
                            loading.hide()
                            val intent = Intent()
                            intent.putExtra(isEdited, true)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun getUser() {
        user = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("user", User::class.java)!!
        else intent.getSerializableExtra("user") as User

    }

}