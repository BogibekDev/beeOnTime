package com.jamesmobiledev.beeontime.model

data class Report(
    val name: String,
    val attendances: ArrayList<Attendance>
)