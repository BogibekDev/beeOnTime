package com.jamesmobiledev.beeontime.extensions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jamesmobiledev.beeontime.utils.write
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun View.getNavigationBarHeight(): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insets =
            rootWindowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
        return insets.bottom
    } else {
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }
}


fun Activity.isKeyboardOpen(): Boolean {
    val rootView = findViewById<View>(android.R.id.content)
    val r = Rect()
    rootView.getWindowVisibleDisplayFrame(r)
    val screenHeight = rootView.height
    val keypadHeight = screenHeight - r.bottom
    return keypadHeight > screenHeight * 0.15
}

fun View.isShow(value: Boolean) {
    if (value) this.show() else this.hide()
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Context.hasPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(this, write) == PackageManager.PERMISSION_GRANTED
    }

}

fun Context.err(it: Exception?) {
    Toast.makeText(this, "${it?.localizedMessage}", Toast.LENGTH_SHORT).show()

}


fun Calendar.byFormat(format: String): String {
    val dateFormat = DateTimeFormatter.ofPattern(format, Locale.getDefault())
    return dateFormat.format(this.toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDate())
}
