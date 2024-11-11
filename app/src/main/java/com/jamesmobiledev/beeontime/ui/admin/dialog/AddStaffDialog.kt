package com.jamesmobiledev.beeontime.ui.admin.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.databinding.ActivityAddStaffBinding
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.UserManager
import com.jamesmobiledev.beeontime.model.User
import com.jamesmobiledev.beeontime.utils.statusUser

class AddStaffDialog : BottomSheetDialogFragment() {
    private lateinit var binding: ActivityAddStaffBinding
    private lateinit var loading: LottieAnimationView
    var onSuccessListener: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityAddStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog?.setOnShowListener { it ->
            val d = it as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading = view.findViewById(R.id.loading)
        initViews()
    }

    private fun initViews() {
        binding.btnLogin.setOnClickListener {
            loading.show()
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()

            if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && lastName.isNotBlank()) {
                val user = User(
                    name = name, lastName = lastName, email = email,
                    password = password, isRequested = false, status = statusUser
                )
                UserManager.save(
                    user = user,
                    onError = {
                        loading.hide()
                        requireContext().err(it)
                    },
                    onSuccess = {
                        loading.hide()
                        Toast.makeText(requireContext(), "Successfully added", Toast.LENGTH_SHORT)
                            .show()
                        onSuccessListener?.invoke()
                        dialog?.dismiss()
                    }
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "email , password and name cannot be empty ",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }

    companion object {
        const val TAG = "AddStaffDialog"
    }
}