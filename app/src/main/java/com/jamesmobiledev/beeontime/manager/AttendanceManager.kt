package com.jamesmobiledev.beeontime.manager

import com.google.firebase.firestore.DocumentSnapshot
import com.jamesmobiledev.beeontime.model.Attendance
import com.jamesmobiledev.beeontime.model.Report
import com.jamesmobiledev.beeontime.utils.ATTENDANCES_PATH
import com.jamesmobiledev.beeontime.utils.biggestTime
import com.jamesmobiledev.beeontime.utils.dateFormatter
import com.jamesmobiledev.beeontime.utils.db
import com.jamesmobiledev.beeontime.utils.differentHours
import com.jamesmobiledev.beeontime.utils.generateDateRange
import com.jamesmobiledev.beeontime.utils.lowestTime
import java.time.LocalDate
import java.util.ArrayList

object AttendanceManager {

    private fun saveAttendance(
        attendance: Attendance,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(ATTENDANCES_PATH)
            .document(attendance.id)
            .set(attendance)
            .addOnFailureListener(onError)
            .addOnSuccessListener { onSuccess.invoke() }
    }


    private fun getAttendances(
        onSuccess: (list: List<Attendance>) -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(ATTENDANCES_PATH).get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    val list = it.result.mapNotNull { document ->
                        documentToAttendance(document)  // Directly map documents to Attendance
                    }
                    onSuccess.invoke(list)
                }
            }
    }


    fun getReportData(
        from: String,
        to: String,
        onError: (Exception?) -> Unit,
        onSuccess: (List<Report>) -> Unit,
    ) {
        val fromDate = LocalDate.parse(from, dateFormatter)
        val toDate = LocalDate.parse(to, dateFormatter)
        val allDates = generateDateRange(fromDate, toDate)
        getAttendances(
            onError = onError,
            onSuccess = { attendances ->
                val reports = attendances
                    .groupBy { it.userName }  // Group by userName
                    .asSequence()  // Use sequence for efficient processing
                    .map { (userName, userAttendances) ->
                        // Create a map of userAttendances by date for fast lookup
                        val userAttendanceByDate = userAttendances.associateBy { it.date }

                        // Map all dates to Attendance, adding "N/A" for missing dates
                        val completeAttendances = allDates.map { date ->
                            userAttendanceByDate[date] ?: Attendance(
                                id = "N/A",
                                uid = "N/A",
                                userName = userName,
                                date = date,
                                inTime = "N/A",
                                outTime = "N/A",
                                check = "N/A",
                                branchId = "N/A",
                                branchName = "N/A",
                                startTime = "N/A",
                                finishTime = "N/A",
                                hours = "N/A"
                            )
                        }

                        Report(userName, ArrayList(completeAttendances))  // Return list instead of ArrayList
                    }
                    .toList()
                onSuccess.invoke(reports)

            }
        )

    }


    private fun getByDate(
        uid: String,
        date: String,
        onSuccess: (Attendance?) -> Unit,
        onError: (Exception?) -> Unit
    ) {
        db.collection(ATTENDANCES_PATH)
            .whereEqualTo("uid", uid)
            .whereEqualTo("date", date)
            .get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    if (it.result.isEmpty) onSuccess.invoke(null)
                    else onSuccess.invoke(documentToAttendance(it.result.first()))
                }

            }

    }

    fun addInTime(
        attendance: Attendance,
        onError: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        getByDate(
            uid = attendance.uid,
            date = attendance.date,
            onError = onError,
            onSuccess = {
                if (it == null) {
                    saveAttendance(
                        attendance = attendance,
                        onError = onError,
                        onSuccess = onSuccess
                    )
                } else {
                    onError.invoke(Exception("You already checked in"))
                }
            }
        )
    }

    fun addOutTime(
        attendance: Attendance,
        onError: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        getByDate(
            uid = attendance.uid,
            date = attendance.date,
            onError = onError,
            onSuccess = {
                if (it == null) onError.invoke(Exception("You have to check in first"))
                else {
                    val hours = differentHours(
                        time1 = biggestTime(it.inTime, it.startTime),
                        time2 = lowestTime(attendance.finishTime, attendance.outTime)
                    )
                    val newA = attendance.copy(
                        id = it.id,
                        inTime = it.inTime,
                        check = it.check,
                        hours = hours
                    )
                    saveAttendance(
                        attendance = newA,
                        onError = onError,
                        onSuccess = onSuccess
                    )
                }
            }
        )

    }

    fun addCheck(
        attendance: Attendance,
        onError: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        getByDate(
            uid = attendance.uid,
            date = attendance.date,
            onError = onError,
            onSuccess = {
                if (it == null) onError.invoke(Exception("You have to checkIn first"))
                else {
                    val newA = attendance.copy(
                        id = it.id,
                        inTime = it.inTime,
                        outTime = it.outTime,
                        hours = it.hours
                    )
                    saveAttendance(
                        attendance = newA,
                        onError = onError,
                        onSuccess = onSuccess
                    )
                }
            }
        )
    }

    private fun documentToAttendance(document: DocumentSnapshot): Attendance {
        return Attendance(
            id = document.getString("id").orEmpty(),
            uid = document.getString("uid").orEmpty(),
            userName = document.getString("userName").orEmpty(),
            date = document.getString("date").orEmpty(),
            inTime = document.getString("inTime").orEmpty(),
            outTime = document.getString("outTime").orEmpty(),
            check = document.getString("check").orEmpty(),
            branchId = document.getString("branchId").orEmpty(),
            branchName = document.getString("branchName").orEmpty(),
            startTime = document.getString("startTime").orEmpty(),
            finishTime = document.getString("finishTime").orEmpty(),
            hours = document.getString("hours").orEmpty()
        )
    }
}