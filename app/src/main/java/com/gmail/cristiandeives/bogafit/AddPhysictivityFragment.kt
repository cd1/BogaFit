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
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.databinding.FragmentAddPhysictivityBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

@MainThread
class AddPhysictivityFragment : Fragment(),
    DatePickerDialog.OnDateSetListener,
    AddPhysictivityActionHandler {

    private lateinit var binding: FragmentAddPhysictivityBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<AddPhysictivityViewModel>()
    private var currentSnackbar: Snackbar? = null

    private val addPhysictivityProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.add_physictivity_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentAddPhysictivityBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            vm = viewModel
            actions = this@AddPhysictivityFragment
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

        viewModel.addPhysictivityStatus.observe(viewLifecycleOwner) { res: Resource<Physictivity>? ->
            Log.v(TAG, "> addPhysictivityState#onChanged(t=$res)")

            when (res) {
                is Resource.Loading -> onAddPhysictivityLoading()
                is Resource.Success -> onAddPhysictivitySuccess()
                is Resource.Error -> onAddPhysictivityError(res)
                is Resource.Canceled -> onAddPhysictivityCanceled()
            }

            if (res?.isFinished == true) {
                addPhysictivityProgressDialog.dismiss()
            }

            Log.v(TAG, "< addPhysictivityState#onChanged(t=$res)")
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

    override fun onAddPhysictivityButtonClick(view: View) {
        Log.i(TAG, "user tapped \"add activity\" button")

        viewModel.addPhysictivity()
    }

    @UiThread
    private fun onAddPhysictivityLoading() {
        addPhysictivityProgressDialog.show()
    }

    @UiThread
    private fun onAddPhysictivitySuccess() {
        navController.navigateUp()
    }

    @UiThread
    private fun onAddPhysictivityError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as AddPhysictivityViewModel.Error) {
                is AddPhysictivityViewModel.Error.InvalidDate -> R.string.add_physictivity_error_invalid_date
                is AddPhysictivityViewModel.Error.Server -> R.string.add_physictivity_error_server
            }

            displayErrorMessage(message)
        }
    }

    @UiThread
    private fun onAddPhysictivityCanceled() {
        // nothing
    }

    @UiThread
    private fun displayErrorMessage(@StringRes messageRes: Int) {
        currentSnackbar = Snackbar.make(requireView(), messageRes, Snackbar.LENGTH_LONG).apply {
            show()
        }
    }

    companion object {
        private val TAG = AddPhysictivityFragment::class.java.simpleName
    }
}
