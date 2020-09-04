package com.gmail.cristiandeives.bogafit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.gmail.cristiandeives.bogafit.databinding.AlertDialogEditMeasurementBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@MainThread
class EditMeasurementDialogFragment : DialogFragment(),
    NumberPicker.OnValueChangeListener,
    DialogInterface.OnClickListener {

    private lateinit var binding: AlertDialogEditMeasurementBinding
    private val args by navArgs<EditMeasurementDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "> onCreateDialog(...)")

        val value0 = if (savedInstanceState?.containsKey(ARG_MEASUREMENT_VALUE0) == true) {
            savedInstanceState.getInt(ARG_MEASUREMENT_VALUE0)
        } else {
            args.value0
        }
        val value1 = if (savedInstanceState?.containsKey(ARG_MEASUREMENT_VALUE1) == true) {
            savedInstanceState.getInt(ARG_MEASUREMENT_VALUE1)
        } else {
            args.value1
        }

        binding = AlertDialogEditMeasurementBinding.inflate(layoutInflater).apply {
            measurementValue0.apply {
                minValue = args.value0Min
                maxValue = args.value0Max
                value = value0
            }
            measurementValue1.apply {
                minValue = args.value1Min
                maxValue = args.value1Max
                value = value1

                setOnValueChangedListener(this@EditMeasurementDialogFragment)
            }

            measurementText0.text = args.text0
            measurementText1.text = args.text1
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.titleRes)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        Log.v(TAG, "< onCreateDialog(...): $dialog")
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.v(TAG, "> onSaveInstanceState(...)")
        super.onSaveInstanceState(outState)

        outState.apply {
            putInt(ARG_MEASUREMENT_VALUE0, binding.measurementValue0.value)
            putInt(ARG_MEASUREMENT_VALUE1, binding.measurementValue1.value)
        }

        Log.v(TAG, "< onSaveInstanceState(...)")
    }

    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        if (oldVal == args.value1Max && newVal == args.value1Min) {
            binding.measurementValue0.value++
        } else if (oldVal == args.value1Min && newVal == args.value1Max) {
            binding.measurementValue0.value--
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        Log.v(TAG, "> onClick(dialog=$dialog, which=$which)")

        setFragmentResult(args.requestKey, bundleOf(
            MeasurementsFragment.BUNDLE_KEY_MEASUREMENT_VALUE0 to binding.measurementValue0.value,
            MeasurementsFragment.BUNDLE_KEY_MEASUREMENT_VALUE1 to binding.measurementValue1.value,
        ))

        Log.v(TAG, "< onClick(dialog=$dialog, which=$which)")
    }

    companion object {
        private val TAG = EditMeasurementDialogFragment::class.java.simpleName

        private const val ARG_MEASUREMENT_VALUE0 = "value0"
        private const val ARG_MEASUREMENT_VALUE1 = "value1"
    }
}