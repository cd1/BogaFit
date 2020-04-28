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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInPasswordBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SignInPasswordFragment : Fragment(),
    DialogInterface.OnClickListener,
    SignInPasswordActionHandler {

    private lateinit var binding: FragmentSignInPasswordBinding
    private val args by navArgs<SignInPasswordFragmentArgs>()
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInPasswordViewModel>(factoryProducer = {
        ViewModelFactory(args.email)
    })

    private val passwordResetProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.reset_password_loading_message))
            setCancelable(false)
        }
    }

    private val signInProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_in_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")
        binding = FragmentSignInPasswordBinding.inflate(inflater, container, false)

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
            action = this@SignInPasswordFragment
        }

        viewModel.apply {
            passwordResetStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> passwordResetStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onPasswordResetLoading()
                    is Resource.Success -> onPasswordResetSuccess()
                    is Resource.Error -> onPasswordResetError(res)
                    is Resource.Canceled -> onPasswordResetCanceled()
                }

                if (res?.isFinished == true) {
                    passwordResetProgressDialog.dismiss()
                }

                Log.v(TAG, "< passwordResetStatus#onChanged(t=$res)")
            }

            signInStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> signInStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onSignInLoading()
                    is Resource.Success -> onSignInSuccess()
                    is Resource.Error -> onSignInError(res)
                    is Resource.Canceled -> onSignInCanceled()
                }

                if (res?.isFinished == true) {
                    signInProgressDialog.dismiss()
                }

                Log.v(TAG, "< signInStatus#onChanged(t=$res)")
            }
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        Log.v(TAG, "> onClick(dialog=$dialog, which=$which)")

        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                Log.i(TAG, "user confirmed reset password dialog")
                viewModel.sendPasswordResetEmail()
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                Log.i(TAG, "user canceled reset password dialog")
            }
        }

        Log.v(TAG, "< onClick(dialog=$dialog, which=$which)")
    }

    override fun onSignInButtonClick(view: View) {
        Log.i(TAG, "user tapped sign in button")

        requireView().hideKeyboard()

        viewModel.signIn()
    }

    override fun onForgotPasswordButtonClick(view: View) {
        Log.i(TAG, "user tapped reset password button")

        requireView().hideKeyboard()

        val dialog = ResetPasswordDialogFragment.newInstance(viewModel.email)
        dialog.show(parentFragmentManager, dialog.toString())
    }

    @UiThread
    private fun onPasswordResetLoading() {
        passwordResetProgressDialog.show()
    }

    @UiThread
    private fun onPasswordResetSuccess() {
        navController.popBackStack()
    }

    @UiThread
    private fun onPasswordResetError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            if (ex is SignInPasswordViewModel.ResetPasswordError.InvalidEmail) {
                onPasswordResetSuccess()
            } else {
                val message = when (ex as SignInPasswordViewModel.ResetPasswordError) {
                    is SignInPasswordViewModel.ResetPasswordError.Server -> R.string.reset_password_error_server
                    else -> null
                }

                message?.let { requireView().showMessage(it) }
            }
        }
    }

    @UiThread
    private fun onPasswordResetCanceled() {
        // nothing
    }

    @UiThread
    private fun onSignInLoading() {
        signInProgressDialog.show()
    }

    @UiThread
    private fun onSignInSuccess() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    @UiThread
    private fun onSignInError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as SignInPasswordViewModel.SignInError) {
                is SignInPasswordViewModel.SignInError.MissingPassword -> R.string.sign_in_error_missing_password
                is SignInPasswordViewModel.SignInError.InvalidCredentials -> R.string.sign_in_error_invalid_credentials
                is SignInPasswordViewModel.SignInError.Server -> R.string.sign_in_error_server
            }

            requireView().showMessage(message)
        }
    }

    @UiThread
    private fun onSignInCanceled() {
        // nothing
    }

    @MainThread
    class ResetPasswordDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            Log.v(TAG, "> onCreateDialog(...)")

            val email = requireArguments().getString(ARG_EMAIL, "")

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_password_confirmation_title)
                .setMessage(getString(R.string.reset_password_message, email))
                .setPositiveButton(R.string.reset_password_positive_button, getListener())
                .setNegativeButton(android.R.string.no, getListener())
                .create()

            Log.v(TAG, "< onCreateDialog(...): $dialog")
            return dialog
        }

        @UiThread
        private fun getListener() =
            parentFragmentManager.primaryNavigationFragment as DialogInterface.OnClickListener

        companion object {
            private val TAG = ResetPasswordDialogFragment::class.java.simpleName
            private const val ARG_EMAIL = "email"

            @UiThread
            fun newInstance(email: String) = ResetPasswordDialogFragment().apply {
                arguments = bundleOf(
                    ARG_EMAIL to email
                )
            }
        }
    }

    private class ViewModelFactory(private val email: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            SignInPasswordViewModel(email) as T
    }

    companion object {
        private val TAG = SignInPasswordFragment::class.java.simpleName
    }
}