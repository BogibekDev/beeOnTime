package com.jamesmobiledev.beeontime.ui.admin.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.jamesmobiledev.beeontime.adapters.AttendanceAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityAttendanceBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.manager.UserManager
import com.jamesmobiledev.beeontime.model.Attendance
import com.jamesmobiledev.beeontime.model.User
import com.jamesmobiledev.beeontime.ui.admin.dialog.FilterAttendanceDialog
import com.jamesmobiledev.beeontime.utils.isEdited
import com.jamesmobiledev.beeontime.utils.isLoaded

class AttendanceActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAttendanceBinding.inflate(layoutInflater) }
    private val adapter by lazy { AttendanceAdapter() }
    private val dialog by lazy { FilterAttendanceDialog() }
    private lateinit var user: User
    private val list = arrayListOf<Attendance>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadList()
        getUser()
        initViews()
    }

    private fun getUser() {
        user = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("user", User::class.java)!!
        else intent.getSerializableExtra("user") as User

    }

    private fun loadList() {
       adapter.submitList(list)
    }

    private fun initViews() {
        setupUnsafeArea()
        binding.apply {
            tvName.text = user.name
            rvAttendance.adapter = adapter
            ivFilter.setOnClickListener {
                }

            ivReport.setOnClickListener {}

            llRemove.setOnClickListener {
                UserManager.deleteUser(
                    user = user,
                    onError = {
                       err(it)
                    },
                    onSuccess = {
                        Toast.makeText(this@AttendanceActivity, "success", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent()
                        intent.putExtra("isLoaded", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                )
            }

            llEdit.setOnClickListener {
                val intent = Intent(this@AttendanceActivity, EditUserActivity::class.java)
                intent.putExtra("user", user)
                editLauncher.launch(intent)
            }
        }
    }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resIntent = result.data
                val isEdited = resIntent?.getBooleanExtra(isEdited, false)
                if (isEdited == true) {
                    val intent = Intent()
                    intent.putExtra(isLoaded, true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }


    private fun setupUnsafeArea() {
        setupStatusBaeArea()
        setupBottomNavBarArea()
    }

    private fun setupBottomNavBarArea() {
        binding.bottomNavBar.doOnLayout {
            val navigationBarHeight = it.getNavigationBarHeight()
            it.updateLayoutParams<LinearLayout.LayoutParams> {
                if (navigationBarHeight != 0) {
                    height = navigationBarHeight
                }
            }
        }
    }

    private fun setupStatusBaeArea() {
        binding.statusBar.layoutParams.also {
            it.height = getStatusBarHeight()
        }

    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = this.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = this.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}