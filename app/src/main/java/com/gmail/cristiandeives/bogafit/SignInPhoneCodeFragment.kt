package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInPhoneCodeBinding

@MainThread
class SignInPhoneCodeFragment : Fragment(),
    SignInPhoneCodeActionHandler {

    private lateinit var binding: FragmentSignInPhoneCodeBinding
    private val viewModel by activityViewModels<SignInViewModel>()

    private val codeVerificationProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_in_phone_check_code_loading))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentSignInPhoneCodeBinding.inflate(inflater, container, false)
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
            action = this@SignInPhoneCodeFragment
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
                codeVerificationProgressDialog.dismiss()
            }

            Log.v(TAG, "< signInStatus#onChanged(t=$res)")
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onVerifyPhoneCodeButtonClick(view: View) {
        Log.i(TAG, "user tapped verify phone button")

        requireView().hideKeyboard()
        viewModel.checkPhoneVerificationCode()
    }

    @UiThread
    private fun onSignInLoading() {
        codeVerificationProgressDialog.show()
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
                is SignInViewModel.Error.InvalidCredentials -> R.string.sign_in_phone_check_code_error
                is SignInViewModel.Error.Server -> R.string.sign_in_error_server
                else -> null
            }

            message?.let { requireView().showMessage(it) }
        }
    }

    @UiThread
    private fun onSignInCanceled() {
        // nothing
    }

    companion object {
        private val TAG = SignInPhoneCodeFragment::class.java.simpleName
    }
}