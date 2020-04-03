package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs

@MainThread
class EditPhysictivityFragment : SavePhysictivityFragment() {
    private val args by navArgs<EditPhysictivityFragmentArgs>()

    override val viewModel by viewModels<EditPhysictivityViewModel>(factoryProducer = {
        ViewModelFactory(requireActivity().application, args.physictivity)
    })

    private val deletePhysictivityProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.save_physictivity_delete_loading_message))
            setCancelable(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        viewModel.deletePhysictivityStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
            Log.v(TAG, "> deletePhysictivityState#onChanged(t=$res)")

            when (res) {
                is Resource.Loading -> onDeletePhysictivityLoading()
                is Resource.Success -> onDeletePhysictivitySuccess()
                is Resource.Error -> onDeletePhysictivityError()
                is Resource.Canceled -> onDeletePhysictivityCanceled()
            }

            if (res?.isFinished == true) {
                deletePhysictivityProgressDialog.dismiss()
            }

            Log.v(TAG, "< deletePhysictivityState#onChanged(t=$res)")
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.v(TAG, "> onCreateOptionsMenu(...)")
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.save_physictivity, menu)

        Log.v(TAG, "< onCreateOptionsMenu(...)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=$item)")

        val handled = when (item.itemId) {
            R.id.delete_item -> {
                onDeleteMenuItemClick()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

        Log.v(TAG, "< onOptionsItemSelected(item=$item): $handled")
        return handled
    }

    @UiThread
    private fun onDeleteMenuItemClick() {
        viewModel.deletePhysictivity()
    }

    @UiThread
    private fun onDeletePhysictivityLoading() {
        deletePhysictivityProgressDialog.show()
    }

    @UiThread
    private fun onDeletePhysictivitySuccess() {
        navController.navigateUp()
    }

    @UiThread
    private fun onDeletePhysictivityError() {
        displayErrorMessage(R.string.save_physictivity_delete_error_server)
    }

    @UiThread
    private fun onDeletePhysictivityCanceled() {
        // nothing
    }

    companion object {
        private val TAG = EditPhysictivityFragment::class.java.simpleName
    }
}