package com.gmail.cristiandeives.bogafit

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.databinding.FragmentSavePhysictivityBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

@MainThread
abstract class SavePhysictivityFragment : Fragment(),
    DatePickerDialog.OnDateSetListener,
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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Log.v(TAG, "> onDateSet(view=..., year=$year, month=$month, dayOfMonth=$dayOfMonth)")

        Log.i(TAG, "user selected date=%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
        viewModel.date.value = LocalDate.of(year, month + 1, dayOfMonth)

        Log.v(TAG, "< onDateSet(view=..., year=$year, month=$month, dayOfMonth=$dayOfMonth)")
    }

    override fun onDateSelectButtonClick(view: View) {
        Log.i(TAG, "user started to select date")

        val date = viewModel.date.value ?: LocalDate.now()

        val action = AddPhysictivityFragmentDirections.toDatePicker(
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
        navController.navigate(action)
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

            displayErrorMessage(message)
        }
    }

    @UiThread
    private fun onSavePhysictivityCanceled() {
        // nothing
    }

    @UiThread
    internal fun displayErrorMessage(@StringRes messageRes: Int) {
        Snackbar.make(requireView(), messageRes, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = SavePhysictivityFragment::class.java.simpleName
    }
}
