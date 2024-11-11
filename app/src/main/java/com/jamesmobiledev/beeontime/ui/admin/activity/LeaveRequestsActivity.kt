package com.jamesmobiledev.beeontime.ui.admin.activity

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.RequestAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityLeaveRequestsBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.RequestManager
import com.jamesmobiledev.beeontime.model.LeaveRequest

class LeaveRequestsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLeaveRequestsBinding.inflate(layoutInflater) }
    private val adapter by lazy { RequestAdapter() }
    private val list = arrayListOf<LeaveRequest>()
    private lateinit var loading: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUnsafeArea()
        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        binding.rvRequests.adapter = adapter
        loadList()
        adapter.onClickAccept = { position ->
            loading.show()
            RequestManager.deleteRequest(
                id = list[position].id,
                uid = list[position].uid,
                onError = {
                    loading.hide()
                    err(it)
                },
                onSuccess = {
                    loading.hide()
                    loadList()
                }
            )
        }

        adapter.onClickReject = { position ->
            loading.show()
            RequestManager.deleteRequest(
                id = list[position].id,
                uid = list[position].uid,
                onError = {
                    loading.hide()
                    err(it)
                },
                onSuccess = {
                    loading.hide()
                    loadList()
                }
            )
        }
    }

    private fun loadList() {
        loading.show()
        RequestManager.loadAll(
            onError = {
                loading.hide()
                err(it)
            },
            onSuccess = {
                list.clear()
                list.addAll(it)
                adapter.submitList(list)
                loading.hide()
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