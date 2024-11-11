package com.jamesmobiledev.beeontime.utils

import android.Manifest
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter

val db by lazy { FirebaseFirestore.getInstance() }

const val USER_PATH = "users"
const val BRANCH_PATH = "branchs"
const val ATTENDANCES_PATH = "attendances"
const val REQUESTS_PATH = "requests"
const val DATE_PATH = "date"
const val statusAdmin = "admin"
const val statusUser = "user"
const val format = "dd.MM.yyyy"

const val isEdited = "isEdited"
const val isLoaded = "isLoaded"
const val write = Manifest.permission.WRITE_EXTERNAL_STORAGE
const val permissionRead = Manifest.permission.READ_EXTERNAL_STORAGE
const val manage = Manifest.permission.MANAGE_EXTERNAL_STORAGE
const val camera = Manifest.permission.CAMERA
const val location = Manifest.permission.ACCESS_FINE_LOCATION

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(format)