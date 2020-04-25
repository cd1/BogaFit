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
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar

@MainThread
class SignUpFragment : Fragment(),
    SignUpActionHandler {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel by viewModels<SignUpViewModel>()

    private val signUpProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_up_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentSignUpBinding.inflate(inflater, container, false)

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
            action = this@SignUpFragment
        }

        viewModel.signUpStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
            Log.v(TAG, "> signUpStatus#onChanged(t=$res)")

            when (res) {
                is Resource.Loading -> onSignUpLoading()
                is Resource.Success -> onSignUpSuccess()
                is Resource.Error -> onSignUpError(res)
                is Resource.Canceled -> onSignUpCanceled()
            }

            if (res?.isFinished == true) {
                signUpProgressDialog.dismiss()
            }

            Log.v(TAG, "< signUpStatus#onChanged(t=$res)")
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onSignUpButtonClick(view: View) {
        Log.i(TAG, "user tapped sign up button")

        viewModel.signUp()
    }

    @UiThread
    private fun onSignUpLoading() {
        signUpProgressDialog.show()
    }

    @UiThread
    private fun onSignUpSuccess() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    @UiThread
    private fun onSignUpError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as SignUpViewModel.Error) {
                is SignUpViewModel.Error.MissingEmail -> R.string.sign_in_error_missing_email
                is SignUpViewModel.Error.InvalidEmail -> R.string.sign_in_error_invalid_email
                is SignUpViewModel.Error.MissingPassword -> R.string.sign_in_error_missing_password
                is SignUpViewModel.Error.Server -> R.string.sign_up_error_server
            }

            displayErrorMessage(message)
        }
    }

    @UiThread
    private fun onSignUpCanceled() {
        // nothing
    }

    @UiThread
    private fun displayErrorMessage(@StringRes messageRes: Int) {
        Snackbar.make(requireView(), messageRes, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = SignUpFragment::class.java.simpleName
    }
}