package com.jamesmobiledev.beeontime.manager

import com.google.firebase.firestore.DocumentSnapshot
import com.jamesmobiledev.beeontime.manager.UserManager.editIsRequested
import com.jamesmobiledev.beeontime.model.LeaveRequest
import com.jamesmobiledev.beeontime.utils.REQUESTS_PATH
import com.jamesmobiledev.beeontime.utils.db

object RequestManager {

    fun add(
        request: LeaveRequest,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(REQUESTS_PATH)
            .document(request.id)
            .set(request)
            .addOnFailureListener(onError)
            .addOnSuccessListener {
                editIsRequested(
                    uid = request.uid,
                    isRequested = true,
                    onError = onError,
                    onSuccess = onSuccess
                )
            }
    }

    fun deleteRequest(
        id: String,
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit
    ) {
        db.collection(REQUESTS_PATH)
            .document(id)
            .delete()
            .addOnFailureListener(onError)
            .addOnSuccessListener {
                editIsRequested(
                    uid = uid,
                    isRequested = false,
                    onError = onError,
                    onSuccess = onSuccess
                )
            }
    }

    fun editRequest(
        id: String,
        status: String,
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit
    ) {
        db.collection(REQUESTS_PATH)
            .document(id)
            .update("status", status)
            .addOnFailureListener(onError)
            .addOnSuccessListener {
                editIsRequested(
                    uid = uid,
                    isRequested = false,
                    onError = onError,
                    onSuccess = onSuccess
                )
            }
    }


    fun loadAll(
        onSuccess: (users: ArrayList<LeaveRequest>) -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(REQUESTS_PATH).get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    val requests = ArrayList<LeaveRequest>()
                    it.result.forEach { document ->
                        val request = documentToRequest(document)
                        requests.add(request)
                    }
                    onSuccess.invoke(requests)
                }
            }
    }


    private fun documentToRequest(document: DocumentSnapshot): LeaveRequest {

        val id = document.getString("id") ?: ""
        val uid = document.getString("uid") ?: ""
        val name = document.getString("name") ?: ""
        val from = document.getString("from") ?: ""
        val to = document.getString("to") ?: ""
        val reason = document.getString("reason") ?: ""
        val status = document.getString("status") ?: ""
        return LeaveRequest(id, uid, name, from, to, reason, status)
    }
}