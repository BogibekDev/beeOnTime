package com.jamesmobiledev.beeontime.ui.admin.activity

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.StaffAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityStaffsBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.UserManager
import com.jamesmobiledev.beeontime.model.User
import com.jamesmobiledev.beeontime.ui.admin.dialog.AddStaffDialog
import com.jamesmobiledev.beeontime.ui.admin.dialog.UserDialog
import com.jamesmobiledev.beeontime.utils.isEdited

class StaffsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityStaffsBinding.inflate(layoutInflater) }
    private val adapter by lazy { StaffAdapter() }
    private lateinit var loading: LottieAnimationView
    private val list = arrayListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUnsafeArea()
        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        loadStaffs()

        binding.fbAddEmployee.setOnClickListener {
            setupBottomSheet()
        }
        binding.ivNotification.setOnClickListener {
            val intent = Intent(this, LeaveRequestsActivity::class.java)
            startActivity(intent)
        }

        binding.rvEmployee.adapter = adapter

        adapter.onClickItem = { position ->
            val dialog = UserDialog(list[position].name ?: "")
            supportFragmentManager.let { dialog.show(it, UserDialog.TAG) }

            dialog.onEditClick = {
                dialog.dismiss()
                editUser(position)
            }

            dialog.onDeleteClick = {
                dialog.dismiss()
                deleteUser(position)
            }

        }
    }

    private fun editUser(position: Int) {
        val intent = Intent(this, EditUserActivity::class.java)
        intent.putExtra("user", list[position])
        launcher.launch(intent)
    }


    private fun deleteUser(position: Int) {
        loading.show()
        UserManager.deleteUser(
            user = list[position],
            onError = {
                loading.hide()
                err(it)
            },
            onSuccess = {
                loading.hide()
                Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show()
                loadStaffs()
            },
        )
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val isEdited = intent?.getBooleanExtra(isEdited, false)
                if (isEdited == true) loadStaffs()
            }
        }

    private fun setupBottomSheet() {
        val dialog = AddStaffDialog()
        supportFragmentManager.let { dialog.show(it, AddStaffDialog.TAG) }
        dialog.onSuccessListener = { loadStaffs() }
    }


    private fun loadStaffs() {
        loading.show()
        UserManager.load(onError = {
            loading.hide()
            err(it)
        }, onSuccess = {
            loading.hide()
            list.clear()
            list.addAll(it)
            adapter.submitList(list)
            setNotifications()
        })
    }


    private fun setNotifications() {
        val isShow = list.any { it.isRequested }
        val color = if (isShow) ContextCompat.getColor(this, R.color.red)
        else ContextCompat.getColor(this, R.color.white)
        binding.ivNotification.setColorFilter(color, PorterDuff.Mode.SRC_IN)
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