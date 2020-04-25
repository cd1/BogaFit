package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInPasswordBinding
import com.google.android.material.snackbar.Snackbar

class SignInPasswordFragment : Fragment(),
    SignInPasswordActionHandler {

    private lateinit var binding: FragmentSignInPasswordBinding
    private val args by navArgs<SignInPasswordFragmentArgs>()
    private val viewModel by viewModels<SignInPasswordViewModel>(factoryProducer = {
        ViewModelFactory(args.email)
    })

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

    private class ViewModelFactory(private val email: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            SignInPasswordViewModel(email) as T
    }

    companion object {
        private val TAG = SignInPasswordFragment::class.java.simpleName
    }
}