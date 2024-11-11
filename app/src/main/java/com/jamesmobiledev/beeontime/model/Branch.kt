package com.jamesmobiledev.beeontime.model

import java.io.Serializable

data class Branch(
    val id: String,
    val name: String,
    val open: String,
    val close: String,
) : Serializable
