package com.gmail.cristiandeives.bogafit

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.databinding.AlertDialogEditDisplayNameBinding
import com.gmail.cristiandeives.bogafit.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@MainThread
class ProfileFragment : Fragment(),
    FragmentResultListener,
    ProfileActionHandler {

    private lateinit var binding: FragmentProfileBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<ProfileViewModel>()

    private val editInfoProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.profile_edit_loading))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val view = binding.root
        Log.v(TAG, "< onCreateView(...): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner

            vm = viewModel
            action = this@ProfileFragment
        }

        lifecycle.addObserver(viewModel)

        viewModel.apply {
            updateDisplayNameStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> updateDisplayNameStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onDisplayNameUpdateLoading()
                    is Resource.Success -> onDisplayNameUpdateSuccess()
                    is Resource.Error -> onDisplayNameUpdateError()
                    is Resource.Canceled -> onDisplayNameUpdateCanceled()
                }

                if (res?.isFinished == true) {
                    editInfoProgressDialog.dismiss()
                }

                Log.v(TAG, "< updateDisplayNameStatus#onChanged(t=$res)")
            }
        }

        childFragmentManager.setFragmentResultListener(REQUEST_KEY_EDIT_DISPLAY_NAME, viewLifecycleOwner, this)

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        Log.v(TAG, "> onFragmentResult(requestKey=$requestKey, result=$result)")

        when (requestKey) {
            REQUEST_KEY_EDIT_DISPLAY_NAME -> {
                val name = result.getString(BUNDLE_KEY_DISPLAY_NAME, "")
                viewModel.updateDisplayName(name)
            }
        }

        Log.v(TAG, "< onFragmentResult(requestKey=$requestKey, result=$result)")
    }

    override fun onDisplayNameTextClick(view: View) {
        Log.i(TAG, "user tapped the display name text")

        val dialog = EditDisplayNameDialogFragment.newInstance(viewModel.displayName)
        dialog.show(childFragmentManager, dialog.toString())
    }

    override fun onPhoneNumberTextClick(view: View) {
        Log.i(TAG, "user tapped the phone text")

        val action = ProfileFragmentDirections.toSignInPhoneNumber()
        navController.navigate(action)
    }

    override fun onSignOutButtonClick(view: View) {
        Log.i(TAG, "user tapped the sign out button")

        viewModel.signOut()

        val intent = Intent(requireContext(), AuthenticationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        startActivity(intent)

        requireActivity().finish()
    }

    @UiThread
    private fun onDisplayNameUpdateLoading() {
        editInfoProgressDialog.show()
    }

    @UiThread
    private fun onDisplayNameUpdateSuccess() {
        // nothing
    }

    @UiThread
    private fun onDisplayNameUpdateError() {
        requireView().showMessage(R.string.profile_edit_name_error)
    }

    @UiThread
    private fun onDisplayNameUpdateCanceled() {
        // nothing
    }

    @MainThread
    class EditDisplayNameDialogFragment : DialogFragment(),
        DialogInterface.OnClickListener {

        private lateinit var binding: AlertDialogEditDisplayNameBinding

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            Log.v(TAG, "> onCreateDialog(...)")

            val bundle = savedInstanceState ?: requireArguments()
            val currentDisplayName = bundle.getString(ARG_DISPLAY_NAME, "")

            binding = AlertDialogEditDisplayNameBinding.inflate(layoutInflater).apply {
                displayName.editText?.setText(currentDisplayName)
            }

            val dialog = MaterialAlertDialogBuilder(requireContext())
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

            outState.putString(ARG_DISPLAY_NAME, binding.displayName.editText?.text.toString())

            Log.v(TAG, "< onSaveInstanceState(...)")
        }

        override fun onClick(dialog: DialogInterface?, which: Int) {
            Log.v(TAG, "> onClick(dialog=$dialog, which=$which)")

            binding.root.hideKeyboard()

            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    setFragmentResult(REQUEST_KEY_EDIT_DISPLAY_NAME, bundleOf(
                        BUNDLE_KEY_DISPLAY_NAME to binding.displayName.editText?.text.toString()
                    ))
                }
            }

            Log.v(TAG, "< onClick(dialog=$dialog, which=$which)")
        }

        companion object {
            private val TAG = EditDisplayNameDialogFragment::class.java.simpleName
            private const val ARG_DISPLAY_NAME = "displayName"

            @UiThread
            fun newInstance(currentDisplayName: String) = EditDisplayNameDialogFragment().apply {
                arguments = bundleOf(
                    ARG_DISPLAY_NAME to currentDisplayName
                )
            }
        }
    }

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName

        private const val REQUEST_KEY_EDIT_DISPLAY_NAME = "editDisplayName"
        private const val BUNDLE_KEY_DISPLAY_NAME = "displayName"
    }
}