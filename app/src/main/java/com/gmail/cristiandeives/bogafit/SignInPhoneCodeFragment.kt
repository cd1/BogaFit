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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInPhoneCodeBinding

@MainThread
class SignInPhoneCodeFragment : Fragment(),
    SignInPhoneCodeActionHandler {

    private lateinit var binding: FragmentSignInPhoneCodeBinding
    private val args by navArgs<SignInPhoneCodeFragmentArgs>()
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInPhoneCodeViewModel>(factoryProducer = {
        ViewModelFactory(args.phoneNumber)
    })

    private val verifyPhoneCodeProgressDialog by lazy {
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

        viewModel.verifyPhoneCodeStatus.observe(viewLifecycleOwner) { res: Resource<SignInPhoneNumberViewModel.SignInReason>? ->
            Log.v(TAG, "> verifyPhoneCodeStatus#onChanged(t=$res)")

            when (res) {
                is Resource.Loading -> onSignInLoading()
                is Resource.Success -> onSignInSuccess(res)
                is Resource.Error -> onSignInError(res)
                is Resource.Canceled -> onSignInCanceled()
            }

            if (res?.isFinished == true) {
                verifyPhoneCodeProgressDialog.dismiss()
            }

            Log.v(TAG, "< verifyPhoneCodeStatus#onChanged(t=$res)")
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onVerifyPhoneCodeButtonClick(view: View) {
        Log.i(TAG, "user tapped verify phone button")

        requireView().hideKeyboard()
        viewModel.checkPhoneVerificationCode(args.phoneVerificationId)
    }

    @UiThread
    private fun onSignInLoading() {
        verifyPhoneCodeProgressDialog.show()
    }

    @UiThread
    private fun onSignInSuccess(res: Resource.Success<SignInPhoneNumberViewModel.SignInReason>) {
        when (res.data) {
            SignInPhoneNumberViewModel.SignInReason.SIGN_IN -> {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
            SignInPhoneNumberViewModel.SignInReason.UPDATE_PHONE_NUMBER -> {
                navController.popBackStack(R.id.profile_fragment, false)
            }
        }
    }

    @UiThread
    private fun onSignInError(res: Resource.Error<*>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as SignInPhoneNumberViewModel.SignInError) {
                is SignInPhoneNumberViewModel.SignInError.InvalidCredentials -> R.string.sign_in_phone_check_code_error_invalid_credentials
                is SignInPhoneNumberViewModel.SignInError.ExistingCredentials -> R.string.sign_in_phone_check_code_error_collision
                is SignInPhoneNumberViewModel.SignInError.Server -> R.string.sign_in_phone_check_code_error_server
            }

            requireView().showMessage(message)
        }
    }

    @UiThread
    private fun onSignInCanceled() {
        // nothing
    }

    private class ViewModelFactory(private val phoneNumber: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            SignInPhoneCodeViewModel(phoneNumber) as T
    }

    companion object {
        private val TAG = SignInPhoneCodeFragment::class.java.simpleName
    }
}