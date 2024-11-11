package com.jamesmobiledev.beeontime.utils

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

fun biggestTime(time1: String, time2: String): String {
    if (time1.isEmpty() || time2.isEmpty()) return ""
    val localTime1 = LocalTime.parse(time1)
    val localTime2 = LocalTime.parse(time2)
    return if (localTime1.isAfter(localTime2)) time1 else time2
}

fun lowestTime(time1: String, time2: String): String {
    if (time1.isEmpty() || time2.isEmpty()) return ""
    val localTime1 = LocalTime.parse(time1)
    val localTime2 = LocalTime.parse(time2)
    return if (localTime1.isBefore(localTime2)) time1 else time2
}

fun differentHours(time1: String, time2: String): String {
    if (time1.isEmpty() || time2.isEmpty()) return "N/A"
    val localTime1 = LocalTime.parse(time1)
    val localTime2 = LocalTime.parse(time2)
    val duration = Duration.between(localTime1, localTime2)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val hour = if (hours.toInt() != 0) "$hours" else "0"
    val minute = if (minutes.toInt() > 9) "$minutes" else "0$minutes"
    return "$hour:$minute"
}

fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<String> {
    return generateSequence(startDate) { date ->
        if (date.isBefore(endDate)) date.plusDays(1) else null
    }.map { it.format(dateFormatter) }
        .toList()
}

