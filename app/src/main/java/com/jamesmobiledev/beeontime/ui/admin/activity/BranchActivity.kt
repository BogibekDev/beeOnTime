package com.jamesmobiledev.beeontime.ui.admin.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.BranchAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityBranchBinding
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.BranchManager
import com.jamesmobiledev.beeontime.model.Branch
import com.jamesmobiledev.beeontime.ui.admin.dialog.UserDialog

class BranchActivity : AppCompatActivity() {
    private val binding by lazy { ActivityBranchBinding.inflate(layoutInflater) }
    private val adapter by lazy { BranchAdapter() }
    private lateinit var loading: LottieAnimationView
    private val list = arrayListOf<Branch>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUnsafeArea()
        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        loadBranches()
        binding.apply {
            rvBranch.adapter = adapter
            fbAddBranch.setOnClickListener {
                val intent = Intent(this@BranchActivity, AddBranchActivity::class.java)
                launcher.launch(intent)
            }

        }
        adapter.onItemClick = { position ->
            val dialog = UserDialog(list[position].name ?: "")
            supportFragmentManager.let { dialog.show(it, UserDialog.TAG) }

            dialog.onEditClick = {
                dialog.dismiss()
                editBranch(position)
            }

            dialog.onDeleteClick = {
                dialog.dismiss()
                deleteBranch(position)
            }
        }
    }

    private fun editBranch(position: Int) {
        val intent = Intent(this, AddBranchActivity::class.java)
        intent.putExtra("branch", list[position])
        launcher.launch(intent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) loadBranches()

        }

    private fun deleteBranch(position: Int) {
        loading.show()
        BranchManager.deleteBranch(
            id = list[position].id,
            onError = {
                loading.hide()
            },
            onSuccess = {
                loading.hide()
                loadBranches()
            }
        )
    }

    private fun loadBranches() {
        loading.show()
        BranchManager.getBranches(
            onError = {
                loading.hide()

            },
            onSuccess = { newList ->
                loading.hide()
                list.clear()
                list.addAll(newList)
                adapter.submitList(list)
            }
        )
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