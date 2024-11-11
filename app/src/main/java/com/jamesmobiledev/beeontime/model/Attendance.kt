package com.jamesmobiledev.beeontime.model

data class Attendance2(
    val date: String,
    val inTime: String,
    val outTime: String,
    val location: String,
    val check: String
)

data class Attendance(
    val id: String,
    val uid: String,
    val userName: String,
    val date: String,
    val inTime: String = "",
    val outTime: String = "",
    val check: String = "",
    val branchId: String,
    val branchName: String,
    val startTime: String,
    val finishTime: String,
    val hours: String=""
)


