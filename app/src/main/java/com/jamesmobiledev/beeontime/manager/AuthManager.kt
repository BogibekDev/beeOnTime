package com.jamesmobiledev.beeontime.manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(
        email: String, password: String,
        onSuccess: (uid: String?) -> Unit, onError: (Exception?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(task.result.user?.uid)
            } else {
                onError(task.exception)
            }
        }
    }


    fun signUp(
        email: String, password: String,
        onSuccess: (uid: String?) -> Unit, onError: (Exception?) -> Unit,
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d("@@@@@", "signUp: task : $task ")
            if (task.isSuccessful) {
                Log.d("@@@@@", "signUp: id: : ${task.result.user?.uid} ")
                onSuccess(task.result.user?.uid)
            } else {
                Log.d("@@@@@", "signUp: exeption : ${task.exception} ")
                onError(task.exception)
            }

        }
    }

    fun editUser(
        oldEmail: String, oldPassword: String,
        newEmail: String, newPassword: String,
        onSuccess: (String?) -> Unit, onError: (Exception?) -> Unit,
    ) {
        logOut()
        //login as a user
        signIn(
            email = oldEmail,
            password = oldPassword,
            onError = onError,
            onSuccess = {
                auth.currentUser?.let { user ->
                    user.verifyBeforeUpdateEmail(newEmail)
                        .addOnFailureListener { onError.invoke(it) }
                        .addOnSuccessListener {
                            user.updatePassword(newPassword)
                                .addOnFailureListener { onError.invoke(it) }
                                .addOnSuccessListener {
                                    loginAsAdmin(
                                        onError = onError,
                                        onSuccess = onSuccess
                                    )
                                }
                        }
                }
            }
        )
    }


    fun deleteUser(
        email: String, password: String,
        onSuccess: () -> Unit, onError: (Exception?) -> Unit,
    ) {
        logOut()
        //login as a user
        signIn(
            email = email,
            password = password,
            onError = onError,
            onSuccess = {
                auth.currentUser?.let { user ->
                    user.delete()
                        .addOnFailureListener { onError.invoke(it) }
                        .addOnSuccessListener {
                            logOut()
                            //login as an admin
                            UserManager.getAdminUser(
                                onError = onError,
                                onSuccess = { user ->
                                    signIn(
                                        email = user.email!!, password = user.password!!,
                                        onError = onError,
                                        onSuccess = { onSuccess.invoke() },
                                    )
                                }
                            )
                        }
                }
            }
        )
    }

    fun createUser(
        email: String, password: String,
        onSuccess: (uid: String?) -> Unit, onError: (Exception?) -> Unit,
    ) {
        logOut()
        signUp(
            email = email,
            password = password,
            onError = onError,
            onSuccess = onSuccess
        )

    }

    fun logOut() = auth.signOut()

    fun loginAsAdmin(
        onSuccess: (uid: String?) -> Unit, onError: (Exception?) -> Unit,
    ) {
        logOut()
        UserManager.getAdminUser(
            onError = onError,
            onSuccess = { admin ->
                signIn(
                    email = admin.email?:"",
                    password = admin.password?:"",
                    onError = onError,
                    onSuccess = onSuccess
                )
            }
        )
    }
}
