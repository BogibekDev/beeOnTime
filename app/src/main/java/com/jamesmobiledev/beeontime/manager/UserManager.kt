package com.jamesmobiledev.beeontime.manager

import com.google.firebase.firestore.DocumentSnapshot
import com.jamesmobiledev.beeontime.model.User
import com.jamesmobiledev.beeontime.utils.USER_PATH
import com.jamesmobiledev.beeontime.utils.db


object UserManager {

    fun save(
        user: User,
        onSuccess: (String?) -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        AuthManager.createUser(
            email = user.email!!,
            password = user.password!!,
            onError = onError,
            onSuccess = { uid ->
                val usr = user.copy(uid = uid)
                store(
                    user = usr,
                    onError = onError,
                    onSuccess = {
                        AuthManager.loginAsAdmin(
                            onError = onError,
                            onSuccess = onSuccess
                        )
                    }
                )
            }
        )
    }

    private fun store(
        user: User,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(USER_PATH).document(user.uid!!).set(user)
            .addOnFailureListener {
                onError.invoke(it)
            }
            .addOnSuccessListener {
                onSuccess.invoke()
            }
    }

    fun editIsRequested(
        uid: String,
        isRequested: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(USER_PATH).document(uid)
            .update("isRequested", isRequested)
            .addOnFailureListener {
                onError.invoke(it)
            }
            .addOnSuccessListener {
                onSuccess.invoke()
            }
    }


    fun load(
        onSuccess: (users: ArrayList<User>) -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(USER_PATH).get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    val users = ArrayList<User>()
                    val documents = it.result.filter { doc -> doc.getString("status") == "user" }
                    documents.forEach { document ->
                        val user = documentToUser(document)
                        users.add(user)
                    }
                    onSuccess.invoke(users)
                }
            }

    }

    fun getById(
        uid: String,
        onSuccess: (User?) -> Unit
    ) {
        db.collection(USER_PATH).document(uid).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess.invoke(documentToUser(it.result))
                } else {
                    onSuccess.invoke(null)
                }
            }

    }

    fun getAdminUser(
        onSuccess: (user: User) -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(USER_PATH).get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    val document = it.result.find { doc -> doc.getString("status") == "admin" }
                    if (document != null) {
                        val user = documentToUser(document)
                        onSuccess.invoke(user)
                    } else onError.invoke(Exception("User is not admin"))
                }

            }
    }

    fun update(
        newUser: User,
        user: User,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        AuthManager.editUser(
            oldEmail = user.email!!,
            newEmail = newUser.email!!,
            oldPassword = user.password!!,
            newPassword = newUser.password!!,
            onError = onError,
            onSuccess = {
                store(
                    user = newUser,
                    onError = onError,
                    onSuccess = onSuccess
                )
            }
        )
    }

    fun deleteUser(
        user: User,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        AuthManager.deleteUser(
            email = user.email!!,
            password = user.password!!,
            onError = onError,
            onSuccess = {
                db.collection(USER_PATH).document(user.uid!!).delete()
                    .addOnFailureListener(onError)
                    .addOnSuccessListener {

                    }

            }
        )


    }


    private fun documentToUser(document: DocumentSnapshot): User {
        val uid = document.getString("uid")
        val name = document.getString("name")
        val email = document.getString("email")
        val status = document.getString("status")
        val password = document.getString("password")
        val isRequested = document.getBoolean("isRequested")
        val lastName = document.getString("lastName").orEmpty()
        return User(
            uid = uid, name = name, lastName = lastName, email = email, password = password,
            status = status ?: "user", isRequested = isRequested ?: false,
        )
    }

}