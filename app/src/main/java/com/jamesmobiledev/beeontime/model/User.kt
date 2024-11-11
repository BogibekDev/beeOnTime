package com.jamesmobiledev.beeontime.model

import java.io.Serializable

data class User(
    val name: String? = null,
    val lastName:String,
    val email: String? = null,
    val password: String? = null,
    val isRequested: Boolean = false,
    val uid: String? = null,
    val status: String = "user"
) : Serializable
