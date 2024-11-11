package com.jamesmobiledev.beeontime.ui.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jamesmobiledev.beeontime.databinding.ActivityImageResultBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.AttendanceManager
import com.jamesmobiledev.beeontime.model.Attendance
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getBranch
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserId
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserName
import com.jamesmobiledev.beeontime.utils.location
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ImageResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageResultBinding
    private var screenshotUri: Uri? = null
    private val latitude = 37.7749 // Replace with your current latitude
    private val longitude = -122.4194

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSafeArea()
        setupBottomSafeArea()


        val imagePath = intent.getStringExtra("image_path")
        val entryType = intent.getStringExtra("entryType")
        val locationName = intent.getStringExtra("locationName")

        if (entryType.equals("CHECK")) {
            binding.imMapFeed.visibility = View.GONE
            binding.btnShare.show()
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
        binding.imageFeed.setImageBitmap(bitmap)

        binding.tvName.text = getUserName(this)
        binding.tvEntryType.text = entryType
        binding.tvLocation.text = locationName
        binding.tvTime.text = getCurrentTime()
        binding.tvDate.text = getCurrentDate()

        if (hasLocationPermission()) getCurrentLocation()


        binding.llAttendance.setOnClickListener {
            val branch = getBranch(this)
            val name = getUserName(this)
            val uid = getUserId(this)
            val id = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            when (entryType) {
                "CHECK" -> {
                    AttendanceManager.addCheck(
                        attendance = Attendance(
                            id = id,
                            uid = uid,
                            userName = name,
                            date = getToday(),
                            inTime = "",
                            outTime = "",
                            check = getCurrentTime(),
                            branchId = branch.id,
                            branchName = branch.name,
                            startTime = branch.open,
                            finishTime = branch.close
                        ),
                        onError = {
                            err(it)
                        },
                        onSuccess = {
                            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }

                "OUT" -> {
                    AttendanceManager.addOutTime(
                        attendance = Attendance(
                            id = id,
                            uid = uid,
                            userName = name,
                            date = getToday(),
                            inTime = "",
                            outTime = getCurrentTime(),
                            check = "",
                            branchId = branch.id,
                            branchName = branch.name,
                            startTime = branch.open,
                            finishTime = branch.close
                        ),
                        onError = {
                            err(it)
                        },
                        onSuccess = {
                            finish()
                            Toast.makeText(this, "successfully added", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                "IN" -> {
                    AttendanceManager.addInTime(
                        attendance = Attendance(
                            id = id,
                            uid = uid,
                            userName = name,
                            date = getToday(),
                            inTime = getCurrentTime(),
                            outTime = "",
                            check = "",
                            branchId = branch.id,
                            branchName = branch.name,
                            startTime = branch.open,
                            finishTime = branch.close
                        ),
                        onError = {
                            err(it)
                        },
                        onSuccess = {
                            finish()
                            Toast.makeText(this, "successfully added", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                else -> Unit
            }
        }

        binding.btnShare.setOnClickListener {
            shareScreenShot()
        }

    }

    private fun setupSafeArea() {
        val layoutParams = binding.frameLayout.layoutParams
        layoutParams.height = getStatusBarHeight()
        binding.frameLayout.layoutParams = layoutParams

    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = this.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = this.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    private fun shareScreenShot() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                saveBitmapToFile(takeScreenshotOfRootView(binding.rootView))
            )
            type = "image/jpeg"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }


    private fun takeScreenshotOfRootView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val tempFile = createTempImageFileInPhotosFolder().apply {
            FileOutputStream(this).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            }
        }
        return FileProvider.getUriForFile(this, "com.jamesmobiledev.beeontime.provider", tempFile)
    }

    private fun createTempImageFileInPhotosFolder(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val photosDir = File(filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        val imageFileName = "Screenshot_${timestamp}"
        return File.createTempFile(imageFileName, ".jpg", photosDir)
    }

    private fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        return timeFormat.format(currentTime)
    }

    private fun getCurrentDate(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun getToday(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun getMapUrlWithMarker(latitude: Double, longitude: Double): String {
        val apiKey = "AIzaSyDK3BylndQJegXdVFzM-lu0ctwgmDezdRE"
        val markerIcon = "https://maps.gstatic.com/mapfiles/api-3/images/spotlight-poi2.png"
        val marker = "markers=icon:$markerIcon|$latitude,$longitude"
        val size = "size=640x480"
        val mapType = "maptype=roadmap"
        Log.d(
            "@@@@",
            "https://maps.googleapis.com/maps/api/staticmap?$marker&$size&$mapType&key=$apiKey"
        )
        return "https://maps.googleapis.com/maps/api/staticmap?$marker&$size&$mapType&key=$apiKey"
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)


        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            Glide.with(this).load(getMapUrlWithMarker(latitude, longitude)).listener(object :
                RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.btnShare.visibility = View.VISIBLE
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        screenshotUri = saveBitmapToFile(takeScreenshotOfRootView(binding.rootView))
//                        if (screenshotUri != null) binding.btnShare.visibility = View.VISIBLE
//                    }, 500)
                    return false
                }

            }).into(binding.imMapFeed)


        } else {
            // Handle the case where location is null
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


    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this, location
    ) == PackageManager.PERMISSION_GRANTED
}