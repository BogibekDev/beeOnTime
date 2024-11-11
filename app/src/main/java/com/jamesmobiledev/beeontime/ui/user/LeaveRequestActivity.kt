package com.jamesmobiledev.beeontime.ui.user

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.adapters.NoFilterArrayAdapter
import com.jamesmobiledev.beeontime.databinding.ActivityLeaveRequestBinding
import com.jamesmobiledev.beeontime.extensions.byFormat
import com.jamesmobiledev.beeontime.extensions.err
import com.jamesmobiledev.beeontime.extensions.getNavigationBarHeight
import com.jamesmobiledev.beeontime.extensions.hide
import com.jamesmobiledev.beeontime.extensions.isKeyboardOpen
import com.jamesmobiledev.beeontime.extensions.show
import com.jamesmobiledev.beeontime.manager.RequestManager
import com.jamesmobiledev.beeontime.model.LeaveRequest
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserId
import com.jamesmobiledev.beeontime.utils.SharedPreferencesHelper.getUserName
import com.jamesmobiledev.beeontime.utils.format
import java.util.Calendar
import java.util.UUID


class LeaveRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaveRequestBinding
    private lateinit var dateFrom: Calendar
    private lateinit var dateTo: Calendar
    private lateinit var loading: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaveRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        loading = findViewById(R.id.loading)
        setupBottomSafeArea()
        setupWindowSoftInput()
        setupReasonDropDown()

        binding.btnFrom.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                { _, year, monthOfYear, dayOfMonth ->
                    dateFrom = Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }

                    binding.btnFrom.text = "$dayOfMonth/${monthOfYear + 1}/$year"

                    binding.btnTo.isEnabled = true
                }, year, month, day

            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            datePickerDialog.show()
        }

        binding.btnTo.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                { _, year, monthOfYear, dayOfMonth ->
                    dateTo = Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }
                    binding.btnTo.text = "$dayOfMonth/${monthOfYear + 1}/$year"
                }, year, month, day

            )

            datePickerDialog.datePicker.minDate = dateFrom.timeInMillis

            datePickerDialog.show()
        }
        binding.apply {
            btnLogin.setOnClickListener {
                if (::dateFrom.isInitialized && ::dateTo.isInitialized && reasonAutoCompleteTV.text.toString()
                        .isNotBlank()
                ) {
                    loading.show()
                    val reason = reasonAutoCompleteTV.text.toString()
                    val from = dateFrom.byFormat(format)
                    val to = dateTo.byFormat(format)
                    val reasonMain = if (reason != "Others") reason
                    else otherReasonAutoCompleteTV.text.toString()
                    val id = UUID.randomUUID().toString()
                    val uid = getUserId(this@LeaveRequestActivity)
                    val name = getUserName(this@LeaveRequestActivity)
                    val request = LeaveRequest(id,uid, name, from, to, reasonMain)
                    RequestManager.add(
                        request = request,
                        onError = {
                            loading.hide()
                            this@LeaveRequestActivity.err(it)
                        },
                        onSuccess = {
                            loading.hide()
                            Toast.makeText(this@LeaveRequestActivity, "Success", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                    )
                } else {
                    Toast.makeText(this@LeaveRequestActivity, "fill all form", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


    }


    private fun setupReasonDropDown() {
        val items = arrayOf(
            "Annual", "Bereavement", "Maternity", "Emergency", "Unpaid", "Sick", "Others"
        )

        val adapter = NoFilterArrayAdapter(this, R.layout.item_drop_down2, items) {
            binding.reasonAutoCompleteTV.setText(it)
            binding.reasonAutoCompleteTV.dismissDropDown()

            Log.d("@@@", "setupReasonDropDown: ${binding.reasonAutoCompleteTV.text.toString()}")

            if (binding.reasonAutoCompleteTV.text.toString() == "Others") {
                binding.otherReasonTextInputLayout.visibility = View.VISIBLE
            } else {
                binding.otherReasonTextInputLayout.visibility = View.INVISIBLE

            }
        }
        binding.reasonAutoCompleteTV.setAdapter(adapter)

        binding.reasonAutoCompleteTV.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                binding.reasonAutoCompleteTV.setText(adapter.getItem(position))
            }

        binding.reasonAutoCompleteTV.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE))
        binding.reasonAutoCompleteTV.setOnClickListener { v -> binding.reasonAutoCompleteTV.showDropDown() }
    }


    private fun setupWindowSoftInput() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {

            val params = binding.linearLayout.layoutParams as ConstraintLayout.LayoutParams

            if (isKeyboardOpen()) {
                params.matchConstraintPercentHeight = 0.95f
            } else {
                params.matchConstraintPercentHeight = 0.75f
            }
            binding.linearLayout.layoutParams = params
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val currentFocusedView = currentFocus
            if (currentFocusedView is EditText) {
                val outRect = Rect()
                currentFocusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
                    imm?.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
                    currentFocusedView.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setupBottomSafeArea() {
        binding.unSafeArea.doOnLayout {
            val navigationBarHeight = it.getNavigationBarHeight()
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (navigationBarHeight != 0) {
                    height = navigationBarHeight
                }
            }
        }
    }


}
