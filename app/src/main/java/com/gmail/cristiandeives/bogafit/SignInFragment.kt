package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar

@MainThread
class SignInFragment : Fragment(),
    SignInActionHandler {

    private lateinit var binding: FragmentSignInBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInViewModel>()

    private val signInProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_in_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")
        binding = FragmentSignInBinding.inflate(inflater, container, false)

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
            action = this@SignInFragment
        }

        viewModel.signInStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
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

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onSignInButtonClick(view: View) {
        Log.i(TAG, "user tapped sign in button")

        viewModel.signIn()
    }

    override fun onSignUpButtonClick(view: View) {
        Log.i(TAG, "user tapped sign up button")

        val action = SignInFragmentDirections.toSignUp()
        navController.navigate(action)
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
            val message = when (ex as SignInViewModel.Error) {
                is SignInViewModel.Error.MissingEmail -> {
                    binding.emailEdit.requestFocus()
                    R.string.sign_in_error_missing_email
                }
                is SignInViewModel.Error.InvalidEmail -> {
                    binding.emailEdit.requestFocus()
                    R.string.sign_in_error_invalid_email
                }
                is SignInViewModel.Error.MissingPassword -> {
                    binding.passwordEdit.requestFocus()
                    R.string.sign_in_error_missing_password
                }
                is SignInViewModel.Error.InvalidCredentials -> R.string.sign_in_error_invalid_credentials
                is SignInViewModel.Error.Server -> R.string.sign_in_error_server
            }

            displayErrorMessage(message)
        }
    }

    @UiThread
    private fun onSignInCanceled() {
        // nothing
    }

    @UiThread
    private fun displayErrorMessage(@StringRes messageRes: Int) {
        Snackbar.make(requireView(), messageRes, Snackbar.LENGTH_LONG).show()
    }


    companion object {
        private val TAG = SignInFragment::class.java.simpleName
    }
}