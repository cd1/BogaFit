package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.databinding.FragmentSavePhysictivityBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@MainThread
abstract class SavePhysictivityFragment : Fragment(),
    MaterialPickerOnPositiveButtonClickListener<Long>,
    SavePhysictivityActionHandler {

    abstract val viewModel: SavePhysictivityViewModel

    private lateinit var binding: FragmentSavePhysictivityBinding
    internal val navController by lazy { findNavController() }

    private val savePhysictivityProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.save_physictivity_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        FragmentSavePhysictivityBinding.inflate(inflater, container, false)
        binding = FragmentSavePhysictivityBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            vm = viewModel
            actions = this@SavePhysictivityFragment
        }

        val view = binding.root
        Log.v(TAG, "< onCreateView(...): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        binding.activitySpinner.adapter = PhysictivityTypeSpinnerAdapter(requireContext()).apply {
            data = Physictivity.Type.values()
        }

        viewModel.apply {
            savePhysictivityStatus.observe(viewLifecycleOwner) { res: Resource<Physictivity>? ->
                Log.v(TAG, "> savePhysictivityStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onSavePhysictivityLoading()
                    is Resource.Success -> onSavePhysictivitySuccess()
                    is Resource.Error -> onSavePhysictivityError(res)
                    is Resource.Canceled -> onSavePhysictivityCanceled()
                }

                if (res?.isFinished == true) {
                    savePhysictivityProgressDialog.dismiss()
                }

                Log.v(TAG, "< savePhysictivityStatus#onChanged(t=$res)")
            }
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onPositiveButtonClick(selection: Long) {
        Log.v(TAG, "> onPositiveButtonClick(selection=$selection)")

        val selectionDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
        Log.i(TAG, "user selected date=$selectionDate")
        viewModel.date.value = selectionDate

        Log.v(TAG, "< onPositiveButtonClick(selection=$selection)")
    }

    override fun onDateSelectButtonClick(view: View) {
        Log.i(TAG, "user started to select date")

        val date = viewModel.date.value ?: LocalDate.now()
        val dateUtcMilli = date.atStartOfDay().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()

        val constraints = CalendarConstraints.Builder()
            .setOpenAt(dateUtcMilli)
            .setEnd(MaterialDatePicker.thisMonthInUtcMilliseconds())
            .setValidator(MaxDateValidator.untilToday())
            .build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(dateUtcMilli)
            .setCalendarConstraints(constraints)
            .build()
        picker.addOnPositiveButtonClickListener(this)
        picker.show(parentFragmentManager, picker.toString())
    }

    override fun onSaveButtonClick(view: View) {
        Log.i(TAG, "user tapped save button")

        viewModel.savePhysictivity()
    }

    @UiThread
    private fun onSavePhysictivityLoading() {
        savePhysictivityProgressDialog.show()
    }

    @UiThread
    private fun onSavePhysictivitySuccess() {
        navController.navigateUp()
    }

    @UiThread
    private fun onSavePhysictivityError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as SavePhysictivityViewModel.Error) {
                is SavePhysictivityViewModel.Error.Server -> R.string.save_physictivity_error_server
            }

            requireView().showMessage(message)
        }
    }

    @UiThread
    private fun onSavePhysictivityCanceled() {
        // nothing
    }

    companion object {
        private val TAG = SavePhysictivityFragment::class.java.simpleName
    }
}
