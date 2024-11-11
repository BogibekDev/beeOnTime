package com.jamesmobiledev.beeontime.ui.admin.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamesmobiledev.beeontime.R

import com.jamesmobiledev.beeontime.databinding.DialogFilterBinding
import java.util.Calendar

class FilterAttendanceDialog : BottomSheetDialogFragment() {
    private lateinit var binding: DialogFilterBinding
    private lateinit var dateFrom: Calendar
    private lateinit var dateTo: Calendar

    var onReportClick: ((Calendar, Calendar) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFilterBinding.inflate(inflater, container, false)
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

        binding.bFilter.setOnClickListener {
            if (::dateFrom.isInitialized && ::dateTo.isInitialized) {
                onReportClick?.invoke(dateFrom, dateTo)
            } else {
                Toast.makeText(requireContext(), "from date and to date set first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFrom.setOnClickListener {
            val c = Calendar.getInstance()
            c.set(2000, 0, 1)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.DatePickerTheme,
                { _, year, monthOfYear, dayOfMonth ->
                    dateFrom = Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }
                    binding.btnFrom.text = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.btnTo.isEnabled = true
                }, year, month, day
            )
            val minDate = Calendar.getInstance().apply { set(2024, 9, 1) }
            datePickerDialog.datePicker.minDate = minDate.timeInMillis

            datePickerDialog.show()
        }

        binding.btnTo.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.DatePickerTheme,
                { _, year, monthOfYear, dayOfMonth ->
                    dateTo = Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }
                    binding.btnTo.text = "$dayOfMonth/${monthOfYear + 1}/$year"
                }, year, month, day

            )

            datePickerDialog.datePicker.minDate = dateFrom.timeInMillis

            datePickerDialog.show()
        }
    }

    companion object {
        val TAG = "FilterDialog"
    }
}