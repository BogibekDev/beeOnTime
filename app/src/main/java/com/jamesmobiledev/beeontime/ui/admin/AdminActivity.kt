package com.jamesmobiledev.beeontime.ui.admin

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.databinding.ActivityAdminBinding
import com.jamesmobiledev.beeontime.extensions.byFormat
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hasPermission
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.AttendanceManager
import com.jamesmobiledev.beeontime.manager.AuthManager
import com.jamesmobiledev.beeontime.model.Attendance
import com.jamesmobiledev.beeontime.model.Report
import com.jamesmobiledev.beeontime.ui.LoginActivity
import com.jamesmobiledev.beeontime.ui.admin.activity.BranchActivity
import com.jamesmobiledev.beeontime.ui.admin.activity.StaffsActivity
import com.jamesmobiledev.beeontime.ui.admin.dialog.FilterAttendanceDialog
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.clearSharedPref
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserName
import com.jamesmobiledev.beeontime.utils.format
import com.jamesmobiledev.beeontime.utils.write
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


class AdminActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAdminBinding.inflate(layoutInflater) }
    private val dialog by lazy { FilterAttendanceDialog() }
    private lateinit var manageExternalStorageLauncher: ActivityResultLauncher<Intent>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var loading: LottieAnimationView
    private val workbook by lazy { XSSFWorkbook() }
    private val list by lazy { arrayListOf<Report>() }
    private val titles = arrayListOf("Branch", "Date", "In", "Out", "Hours")
    private lateinit var sheet: Sheet
    private lateinit var boldStyle: XSSFCellStyle
    private lateinit var greenStyle: XSSFCellStyle
    private lateinit var normalStyle: XSSFCellStyle

    private val code = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePermissionLaunchers()
        setContentView(binding.root)
        if (!this.hasPermission()) requestStoragePermission()
        setupUnsafeArea()
        initViews()
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            try {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:" + applicationContext.packageName)
                    manageExternalStorageLauncher.launch(intent)
                }
            } catch (e: Exception) {
                // Handle case where the intent is not available on this device
                Log.e("Permissions", "Manage all files intent not supported", e)
                // You can show a message to the user or fallback to app-specific directories
            }
        } else {
            // For Android 10 and below
            storagePermissionLauncher.launch(arrayOf(write))
        }
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        binding.apply {
            tvName.text = getUserName(this@AdminActivity)
            llstafandbranch.layoutParams.height = getScreenWidth() / 2
            llRepost.layoutParams.height = getScreenWidth() / 4
            llStaff.setOnClickListener {
                startActivity(Intent(this@AdminActivity, StaffsActivity::class.java))
            }

            llRepost.setOnClickListener {
                supportFragmentManager.let { dialog.show(it, FilterAttendanceDialog.TAG) }
                dialog.onReportClick = { from, to ->
                    dialog.dismiss()
                    getReports(from.byFormat(format), to.byFormat(format))
                }
            }
            llBranch.setOnClickListener {
                startActivity(Intent(this@AdminActivity, BranchActivity::class.java))

            }

            btnLogOut.setOnClickListener {
                logoutUser()
            }
        }
    }


    private fun getReports(from: String, to: String) {
        loading.show()
        AttendanceManager.getReportData(
            from = from,
            to = to,
            onError = {
                loading.hide()
                err(it)
            },
            onSuccess = {
                list.clear()
                list.addAll(it)
                createSheet(from, to)
            }
        )
    }


    private fun initializePermissionLaunchers() {
        // For Android 11 and above (manage all files permission)
        manageExternalStorageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                    Log.d("@@@@@", "Manage external storage permission granted")
                } else {
                    // Permission denied
                    Log.e("@@@@@", "Manage external storage permission denied")
                }
            }
        }

        // For Android 10 and below (WRITE_EXTERNAL_STORAGE permission)
        storagePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[write] == true) {
                // Permission granted
                Log.d("@@@@@", "WRITE_EXTERNAL_STORAGE permission granted")
            } else {
                // Permission denied
                Log.e("@@@@@", "WRITE_EXTERNAL_STORAGE permission denied")
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == code) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permision is granted", Toast.LENGTH_SHORT).show()
            } else {
                requestStoragePermission()
            }
        }
    }


    private fun createSheet(from: String, to: String) {
        try {
            sheet = workbook.createSheet("Report")
            val numberOfLines = 1
            val estimatedHeight = (numberOfLines * (16 * 1.2)).toFloat()
            sheet.defaultRowHeightInPoints = estimatedHeight
            sheet.defaultColumnWidth = (20 + 2) * 256
            initStyles()

            reportMainTitle(from, to)
            var rowIndex = 4

            list.forEach { report ->
                writeTitle(report.name, rowIndex++)
                report.attendances.forEach { attendance: Attendance ->
                    writeRow(rowIndex++, attendance)
                }
                rowIndex++
            }

            writeFile()


        } catch (e: Exception) {
            Log.d("@@@@@", "createSheet: ${e.localizedMessage}")
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        } finally {
            workbook.close()

        }
    }

    private fun reportMainTitle(from: String, to: String) {
        var row = sheet.createRow(0)
        val cell = row.createCell(1)
        cell.setCellValue("Report")
        cell.cellStyle = boldStyle

        // Define header titles and values
        val headerData = listOf(
            "From" to from,
            "To" to to
        )

        // Iterate over header data and create rows and cells
        headerData.forEachIndexed { index, (header, value) ->
            row = sheet.createRow(index + 1) // Start at row 1 for headers
            row.createCell(1).apply {
                setCellValue(header)
                cellStyle = boldStyle
            }
            row.createCell(2).apply {
                setCellValue(value)
                cellStyle = boldStyle
            }
        }

    }

    private fun initStyles() {
        val font = workbook.createFont().apply {
            fontHeightInPoints = 14
            fontName = "Times New Roman"
        }
        val boldFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.BLACK.index
            fontHeightInPoints = 16
            fontName = "Times New Roman"
        }

        normalStyle = workbook.createCellStyle().apply {
            setFont(font)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        boldStyle = workbook.createCellStyle().apply {
            setFont(boldFont)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        greenStyle = workbook.createCellStyle().apply {
            setFont(font)
            fillForegroundColor = IndexedColors.GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
    }

    private fun writeRow(rowIndex: Int, attendance: Attendance) {
        // Create the row for the given index
        val row = sheet.createRow(rowIndex)

        // Create a list of attendance properties
        val attendanceValues = listOf(
            attendance.branchName,
            attendance.date,
            attendance.inTime,
            attendance.outTime,
            attendance.check
        )

        // Iterate over the attendance values and create cells
        for ((index, value) in attendanceValues.withIndex()) {
            val cell = row.createCell(index + 3) // Start at index 3
            cell.setCellValue(value)
            cell.cellStyle = normalStyle
        }
    }

    private fun writeTitle(staffName: String, rowIndex: Int) {

        var cell: Cell

        var index = 2
        val row = sheet.createRow(rowIndex)

        cell = row.createCell(index++)
        cell.setCellValue(staffName)
        cell.cellStyle = boldStyle

        titles.forEach {
            cell = row.createCell(index++)
            cell.setCellValue(it)

            cell.cellStyle = boldStyle
        }
    }

    private fun writeFile() {
        val fileName = "${System.currentTimeMillis()}.xlsx"
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val reportsDir = File(path, "BeeOnTime/reports")

        if (!reportsDir.exists()) reportsDir.mkdirs()

        val file = File(reportsDir, fileName)

        try {
            FileOutputStream(file).use {
                workbook.write(it)
                loading.hide()
                Toast.makeText(this, "Files/Documents/BeeOnTime/reports", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.d("@@@@@", e.localizedMessage ?: "")
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


    private fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels


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

    private fun logoutUser() {
        AuthManager.logOut()
        clearSharedPref(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}