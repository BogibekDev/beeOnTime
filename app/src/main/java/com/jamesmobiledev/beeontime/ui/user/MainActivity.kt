package com.jamesmobiledev.beeontime.ui.user

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.LocationDropDownAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityMainBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.isKeyboardOpen
import com.jamesmobiledev.beeontime.manager.AuthManager
import com.jamesmobiledev.beeontime.manager.BranchManager
import com.jamesmobiledev.beeontime.model.Branch
import com.jamesmobiledev.beeontime.ui.LoginActivity
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.clearSharedPref
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserEmails
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveBranch
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.saveUserEmails
import com.jamesmobiledev.beeontime.utils.camera
import com.jamesmobiledev.beeontime.utils.location


class MainActivity : AppCompatActivity() {
    private var lastBackPressedTime: Long = 0
    private lateinit var backToast: Toast
    private lateinit var binding: ActivityMainBinding
    private var locationList: ArrayList<String> = ArrayList()
    private var branches = arrayListOf<Branch>()
    private var selected = -1

    private lateinit var adapter: LocationDropDownAdapter
    private val LOCATION_PERMISSION_REQUEST_CODE = 124
    private val CAMERA_PERMISSION_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }


    private fun initViews() {
        setupBottomSafeArea()
        setupWindowSoftInput()
        setupLocationAutoCompleteView()

        binding.btnIn.setOnClickListener {

            navigateToCameraActivity("IN")
        }

        binding.btnOut.setOnClickListener {
            navigateToCameraActivity("OUT")
        }

        binding.btnCheck.setOnClickListener {
            navigateToCameraActivity("CHECK")

        }

        binding.btnLeaveRequest.setOnClickListener {
            startActivity(Intent(this, LeaveRequestActivity::class.java))
        }

        binding.btnLogOut.setOnClickListener {
            logoutUser()
        }


    }

    private fun getBranches() {
        BranchManager.getBranches(
            onError = { err(it) },
            onSuccess = { list ->
                locationList.clear()
                branches.clear()
                locationList.addAll(list.map { it.name })
                branches.addAll(list)
                Log.d("@@@@@", "getBranches: $locationList")
            }
        )
    }

    private fun navigateToCameraActivity(entryType: String) {
        if (!permissionsGranted()) return

        if (binding.locationAutoCompleteTV.text.toString().isNotBlank()) {
            saveBranch(this, branches[selected])

            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("entryType", entryType)
            intent.putExtra("locationName", binding.locationAutoCompleteTV.text.toString())
            startActivity(intent)
        } else {
            Toast.makeText(this, "The Location cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun permissionsGranted(): Boolean {
        val needPermissions = arrayListOf<String>()

        if (hasCameraPermission() && hasLocationPermission()) return true

        if (isCameraPermissionDenied()) showSettingsDialog("Camera")
        else if (!hasCameraPermission()) needPermissions.add(camera)

        if (isLocationPermissionDenied()) showSettingsDialog("Location")
        else if (!hasLocationPermission()) needPermissions.add(location)

        if (needPermissions.isNotEmpty()) requestMultiplePermissions.launch(needPermissions.toTypedArray())
        return false
    }

    private fun setupLocationAutoCompleteView() {
        adapter = LocationDropDownAdapter(this, R.layout.item_drop_down, locationList) {
            selected = it
            binding.locationAutoCompleteTV.setText(branches[it].name, false)

            binding.locationAutoCompleteTV.dismissDropDown()
        }
        binding.locationAutoCompleteTV.setAdapter(adapter)


    }

    override fun onResume() {
        super.onResume()
        binding.locationAutoCompleteTV.setText("", false)
        getBranches()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (lastBackPressedTime + 1000 > System.currentTimeMillis()) {
            backToast.cancel() // Cancel the previous toast to avoid toast queue
            super.onBackPressed()
            return
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
            backToast.show()
        }
        lastBackPressedTime = System.currentTimeMillis()
    }


    private fun showSettingsDialog(permission: String) {
        val message =
            "$permission permission is essential for this app. Please open settings and allow the permission."

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showPermissionRequiredToast(permission)
            }.create().show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            val uri = Uri.fromParts("package", packageName, null)
            data = uri
        }
        settingsLauncher.launch(intent)
    }


    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private fun showPermissionRequiredToast(permission: String) {
        Toast.makeText(this, "$permission permission is required", Toast.LENGTH_LONG).show()
    }


    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, camera) == PackageManager.PERMISSION_GRANTED

    private fun isCameraPermissionDenied() =
        ActivityCompat.shouldShowRequestPermissionRationale(this, camera)


    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this, location
    ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationPermissionDenied() = ActivityCompat.shouldShowRequestPermissionRationale(
        this, location
    )


    private fun logoutUser() {
        AuthManager.logOut()
        val emails = getUserEmails(this)
        clearSharedPref(this)
        emails.forEach { saveUserEmails(this, it) }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                Toast.makeText(this, "$permissionName granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$permissionName denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}