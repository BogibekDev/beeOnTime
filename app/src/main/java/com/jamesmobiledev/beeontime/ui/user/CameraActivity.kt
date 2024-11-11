package com.jamesmobiledev.beeontime.ui.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.google.common.util.concurrent.ListenableFuture
import com.jamesmobiledev.beeontime.databinding.ActivityCameraBinding
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var cameraProviderFuture:  ListenableFuture<ProcessCameraProvider>? = null
    private var deviceOrientation: Int = 0
    private var orientationEventListener: OrientationEventListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            binding.captureButton.isEnabled = true
            binding.btnClose.isEnabled = true
            startCamera()
        } else {
            Toast.makeText(this, "Camera Permission is not granted", Toast.LENGTH_SHORT).show()
            this.finish()
        }
    }

    override fun onStop() {
        super.onStop()
        cameraProviderFuture?.get()?.unbindAll()
        orientationEventListener?.disable()
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture?.let { future ->
            future.addListener({
                val cameraProvider: ProcessCameraProvider = future.get()

                val preview = Preview.Builder().setTargetResolution(Size(1500, 2000)).build().also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().setTargetResolution(Size(1500, 2000)).build()

                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                    orientationEventListener = object : OrientationEventListener(this) {
                        override fun onOrientationChanged(orientation: Int) {
                            deviceOrientation = orientation
                        }
                    }
                    orientationEventListener?.enable()
                } catch (exc: Exception) {
                    Toast.makeText(this, "Use case binding failed", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

            }, ContextCompat.getMainExecutor(this))
        }

    }

    private fun takePhoto() {
        binding.flashView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.flashView.visibility = View.GONE
        }, 500)

        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Error occurred while capturing a photo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    val tempFile = createTempImageFileInPhotosFolder().apply {
                        FileOutputStream(this).use { out ->
                            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }
                    }

                    val intent =
                        Intent(this@CameraActivity, ImageResultActivity::class.java).apply {
                            putExtra("entryType", intent.getStringExtra("entryType"))
                            putExtra("locationName", intent.getStringExtra("locationName"))
                            putExtra("image_path", tempFile.absolutePath)
                        }
                    startActivity(intent)
                    finish()
                }

            })
    }

    private fun createTempImageFileInPhotosFolder(): File {
        clearAllImagesFromPhotosFolder()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val photosDir = File(filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        val imageFileName = "JPEG_${timestamp}"
        return File.createTempFile(imageFileName, ".jpg", photosDir)
    }

    private fun clearAllImagesFromPhotosFolder() {
        val photosDir = File(filesDir, "photos")
        if (photosDir.exists() && photosDir.isDirectory) {
            val files = photosDir.listFiles()

            files?.let {
                for (file in it) {
                    file.delete()
                }
            }
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


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val rotationDegrees = when (deviceOrientation) {
            in 45..134 -> 180 // Landscape
            in 135..224 -> 90// Upside down
            in 225..314 -> 0  // Landscape reverse
            else -> 270 // Portrait
        }

        return if (rotationDegrees == 0) {
            bitmap
        } else {
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }


    private fun initViews() {
        setupBottomSafeArea()
        binding.captureButton.setOnClickListener {
            if (intent.getStringExtra("entryType").equals("CHECK")) {
                startCountdown()
            } else {
                takePhoto()
            }
            binding.captureButton.isEnabled = false
            binding.btnClose.isEnabled = false
        }

        binding.btnClose.setOnClickListener {
            this.finish()
        }

    }

    private fun startCountdown() {
        binding.tvCount.visibility = View.VISIBLE

        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCount.text = ((millisUntilFinished / 1000).toInt() + 1).toString()
            }

            override fun onFinish() {
                binding.tvCount.visibility = View.GONE
                takePhoto()
            }
        }.start()
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED


}