package com.gmail.cristiandeives.bogafit

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs

@MainThread
class DatePickerDialogFragment : DialogFragment() {
    private val args by navArgs<DatePickerDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "> onCreateDialog(...)")

        val dialog = DatePickerDialog(
            requireContext(),
            parentFragmentManager.primaryNavigationFragment as DatePickerDialog.OnDateSetListener,
            args.year,
            args.month,
            args.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }

        Log.v(TAG, "< onCreateDialog(...): $dialog")
        return dialog
    }

    companion object {
        private val TAG = DatePickerDialogFragment::class.java.simpleName
    }
}