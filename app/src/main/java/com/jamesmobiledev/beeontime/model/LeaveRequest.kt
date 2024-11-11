package com.jamesmobiledev.beeontime.model

data class LeaveRequest(
    val id:String,
    val uid: String,
    val name: String,
    val from: String,
    val to: String,
    val reason: String,
    val status: String = none
) {
    companion object STATUS {
        const val none = "None"
        const val rejected = "Rejected"
        const val accepted = "Accepted"
    }
}
