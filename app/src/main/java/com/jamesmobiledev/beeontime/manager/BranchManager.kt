package com.jamesmobiledev.beeontime.manager

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.jamesmobiledev.beeontime.model.Branch
import com.jamesmobiledev.beeontime.utils.BRANCH_PATH
import com.jamesmobiledev.beeontime.utils.db

object BranchManager {

    fun storeBranch(
        branch: Branch,
        onSuccess: () -> Unit,
        onError: (Exception?) -> Unit,
    ) {
        db.collection(BRANCH_PATH).document(branch.id).set(branch)
            .addOnFailureListener {
                onError.invoke(it)
            }
            .addOnSuccessListener {
                onSuccess.invoke()
            }

    }

    fun getBranches(
        onSuccess: (ArrayList<Branch>) -> Unit,
        onError: (Exception?) -> Unit,
    ) {

        db.collection(BRANCH_PATH).get()
            .addOnCompleteListener {
                if (!it.isSuccessful) onError.invoke(it.exception)
                else {
                    val list = ArrayList<Branch>()
                    it.result.forEach { document ->
                        if (document != null) list.add(documentToBranch(document))
                    }
                    onSuccess.invoke(list)
                }
            }
    }

    fun deleteBranch(
        id: String,
        onError: (Exception?) -> Unit,
        onSuccess: () -> Unit,
    ) {
        db.collection(BRANCH_PATH).document(id).delete()
            .addOnFailureListener { onError.invoke(it) }
            .addOnSuccessListener { onSuccess.invoke() }
    }


    private fun documentToBranch(document: QueryDocumentSnapshot): Branch {
        return Branch(
            id = document.getString("id") ?: "",
            name = document.getString("name") ?: "",
            open = document.getString("open") ?: "",
            close = document.getString("close") ?: "",
        )
    }
}