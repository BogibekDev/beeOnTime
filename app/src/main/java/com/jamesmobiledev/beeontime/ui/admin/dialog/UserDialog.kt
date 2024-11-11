package com.jamesmobiledev.beeontime.ui.admin.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamesmobiledev.beeontime.databinding.DialogUserBinding

class UserDialog(private val name: String) : BottomSheetDialogFragment() {
    private lateinit var binding: DialogUserBinding
    var onEditClick: (() -> Unit)? = null
    var onDeleteClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUserBinding.inflate(inflater, container, false)
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
        initViews()
    }

    private fun initViews() {

        binding.tvName.text = name
        binding.llEdit.setOnClickListener {
            onEditClick?.invoke()
        }
        binding.llRemove.setOnClickListener {
            onDeleteClick?.invoke()
        }
    }

    companion object {
        val TAG = "UserDialog"
    }
}