package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
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
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInBinding

@MainThread
class SignInMainFragment : Fragment(),
    SignInMainActionHandler {

    private lateinit var binding: FragmentSignInBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInViewModel>()

    private val phoneVerificationProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sign_in_phone_verification_loading))
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
            action = this@SignInMainFragment
        }

        viewModel.phoneVerificationStatus.observe(viewLifecycleOwner) { event: Event<Resource<*>>? ->
            Log.v(TAG, "> phoneVerificationStatus#onChanged(t=$event)")

            val res = event?.peek()?.takeIf { !event.consumed }

            when (res) {
                is Resource.Loading -> onPhoneVerificationLoading()
                is Resource.Success -> onPhoneVerificationSuccess()
                is Resource.Error -> onPhoneVerificationError()
            }

            if (res?.isFinished == true) {
                phoneVerificationProgressDialog.dismiss()
            }

            when (res) {
                is Resource.Success, is Resource.Error -> event.consume()
            }

            Log.v(TAG, "< phoneVerificationStatus#onChanged(t=$event)")
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onPhoneNextButtonClick(view: View) {
        Log.i(TAG, "user tapped phone next button")

        requireView().hideKeyboard()
        viewModel.verifyPhoneNumber(requireActivity())
    }

    override fun onEmailNextButtonClick(view: View) {
        Log.i(TAG, "user tapped e-mail next button")

        val action = SignInMainFragmentDirections.toSignInPassword(viewModel.email.value.orEmpty())
        navController.navigate(action)
    }

    override fun onSignUpButtonClick(view: View) {
        Log.i(TAG, "user tapped sign up button")

        val action = SignInMainFragmentDirections.toSignUp()
        navController.navigate(action)
    }

    @UiThread
    private fun onPhoneVerificationLoading() {
        phoneVerificationProgressDialog.show()
    }

    @UiThread
    private fun onPhoneVerificationSuccess() {
        val action = SignInMainFragmentDirections.toSignInPhoneCode()
        navController.navigate(action)
    }

    @UiThread
    private fun onPhoneVerificationError() {
        requireView().showMessage(R.string.sign_in_phone_verification_error)
    }

    companion object {
        private val TAG = SignInMainFragment::class.java.simpleName
    }
}