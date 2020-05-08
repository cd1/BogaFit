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
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInPhoneNumberBinding

@MainThread
class SignInPhoneNumberFragment : Fragment(),
    SignInPhoneNumberActionHandler {

    private lateinit var binding: FragmentSignInPhoneNumberBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInPhoneNumberViewModel>()

    private val verifyPhoneProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_in_phone_verification_loading))
            setCancelable(false)
        }
    }

    private val signInProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.profile_edit_loading))
            setCancelable(false)
        }
    }

    private val removePhoneNumberProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.profile_remove_phone_number_loading))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentSignInPhoneNumberBinding.inflate(inflater, container, false)

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
            action = this@SignInPhoneNumberFragment
        }

        viewModel.apply {
            verifyPhoneStatus.observe(viewLifecycleOwner) { res: Resource<Event<String>>? ->
                Log.v(TAG, "> verifyPhoneStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onVerifyPhoneLoading()
                    is Resource.Success -> onVerifyPhoneSuccess(res)
                    is Resource.Error -> onVerifyPhoneError()
                }

                if (res?.isFinished == true) {
                    verifyPhoneProgressDialog.dismiss()
                }

                Log.v(TAG, "< verifyPhoneStatus#onChanged(t=$res)")
            }

            signInStatus.observe(viewLifecycleOwner) { res: Resource<SignInPhoneNumberViewModel.SignInReason>? ->
                Log.v(TAG, "> signInStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onSignInLoading()
                    is Resource.Success -> onSignInSuccess(res)
                    is Resource.Error -> onSignInError(res)
                    is Resource.Canceled -> onSignInCanceled()
                }

                if (res?.isFinished == true) {
                    signInProgressDialog.dismiss()
                }

                Log.v(TAG, "< signInStatus#onChanged(t=$res)")
            }

            removePhoneNumberStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> removePhoneNumberStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onRemovePhoneNumberLoading()
                    is Resource.Success -> onRemovePhoneNumberSuccess()
                    is Resource.Error -> onRemovePhoneNumberError()
                    is Resource.Canceled -> onRemovePhoneNumberCanceled()
                }

                if (res?.isFinished == true) {
                    removePhoneNumberProgressDialog.dismiss()
                }

                Log.v(TAG, "< removePhoneNumberStatus#onChanged(t=$res)")
            }
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onNextButtonClick(view: View) {
        Log.i(TAG, "user tapped next button")

        requireView().hideKeyboard()
        viewModel.verifyPhoneNumber(requireActivity())
    }

    override fun onRemovePhoneNumberButtonClick(view: View) {
        Log.i(TAG, "user tapped remove phone number button")

        if (viewModel.canRemovePhoneNumber()) {
            requireView().showMessage(R.string.profile_cannot_remove_phone_number_message)
            return
        }

        viewModel.removePhoneNumber()
    }

    @UiThread
    private fun onVerifyPhoneLoading() {
        verifyPhoneProgressDialog.show()
    }

    @UiThread
    private fun onVerifyPhoneSuccess(res: Resource.Success<Event<String>>) {
        if (res.data?.consume() != null) {
            val phoneNumber = viewModel.phoneNumber.value.orEmpty()
            val phoneVerificationId = viewModel.phoneVerificationId.orEmpty()

            val action = SignInPhoneNumberFragmentDirections.toSignInPhoneCode(phoneNumber, phoneVerificationId)
            navController.navigate(action)
        }
    }

    @UiThread
    private fun onVerifyPhoneError() {
        requireView().showMessage(R.string.sign_in_phone_verification_error)
    }

    @UiThread
    private fun onSignInLoading() {
        signInProgressDialog.show()
    }

    @UiThread
    private fun onSignInSuccess(res: Resource.Success<SignInPhoneNumberViewModel.SignInReason>) {
        when (res.data) {
            SignInPhoneNumberViewModel.SignInReason.SIGN_IN -> {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
            SignInPhoneNumberViewModel.SignInReason.UPDATE_PHONE_NUMBER -> {
                navController.popBackStack()
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

    @UiThread
    private fun onRemovePhoneNumberLoading() {
        removePhoneNumberProgressDialog.show()
    }

    @UiThread
    private fun onRemovePhoneNumberSuccess() {
        navController.popBackStack()
    }

    @UiThread
    private fun onRemovePhoneNumberError() {
        requireView().showMessage(R.string.profile_remove_phone_number_error)
    }

    @UiThread
    private fun onRemovePhoneNumberCanceled() {
        // nothing
    }

    companion object {
        private val TAG = SignInPhoneNumberFragment::class.java.simpleName
    }
}