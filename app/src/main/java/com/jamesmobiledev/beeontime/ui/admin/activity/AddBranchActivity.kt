package com.jamesmobiledev.beeontime.ui.admin.activity

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.databinding.ActivityAddBranchBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.BranchManager
import com.jamesmobiledev.beeontime.model.Branch
import java.util.UUID


class AddBranchActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddBranchBinding.inflate(layoutInflater) }
    private lateinit var loading: LottieAnimationView
    private var branch: Branch? = null
    private lateinit var id: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        getBranch()
        loading = findViewById(R.id.loading)
        binding.apply {
            id = UUID.randomUUID().toString()
            branch?.let {
                id = it.id
                tvTitle.text = "Edit"
                nameEditText.setText(it.name)
                openEditText.setText(it.open)
                closeEditText.setText(it.close)
            }

            openEditText.setOnClickListener {
                showTimePicker(true)
            }

            closeEditText.setOnClickListener {
                showTimePicker(false)
            }

            btnLogin.setOnClickListener {
                val name = nameEditText.text.toString()
                val open = openEditText.text.toString()
                val close = closeEditText.text.toString()
                if (open.isBlank() || close.isBlank() || name.isBlank()) {
                    Toast.makeText(this@AddBranchActivity, "pustoy", Toast.LENGTH_SHORT).show()
                } else {
                    loading.show()
                    branch = Branch(id, name, open, close)
                    BranchManager.storeBranch(
                        branch = branch!!,
                        onError = {
                            loading.hide()
                            err(it)
                        },
                        onSuccess = {
                            Toast.makeText(
                                this@AddBranchActivity,
                                "Successfully added",
                                Toast.LENGTH_SHORT
                            ).show()
                            loading.hide()
                            val intent = Intent()
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun getBranch() {
        branch = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("branch", Branch::class.java)
        else intent.getSerializableExtra("branch") as Branch?
    }


    private fun showTimePicker(isOpen: Boolean) {
        val timePickerDialog = TimePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog,
            { timePicker, selectedHour, selectedMinute ->
                val hour = if (selectedHour < 10) "0$selectedHour" else "$selectedHour"
                val minute = if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute"
                if (isOpen) binding.openEditText.setText("$hour:$minute")
                else binding.closeEditText.setText("$hour:$minute")
            },
            if (isOpen) 9 else 18,
            0, true
        )
        timePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timePickerDialog.setTitle("Select time")
        timePickerDialog.show()

    }
}