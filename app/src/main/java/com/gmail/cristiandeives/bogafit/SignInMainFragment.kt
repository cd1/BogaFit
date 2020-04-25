package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.databinding.FragmentSignInBinding

@MainThread
class SignInMainFragment : Fragment(),
    SignInMainActionHandler {

    private lateinit var binding: FragmentSignInBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<SignInViewModel>()

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

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onNextButtonClick(view: View) {
        Log.i(TAG, "user tapped next button")

        val action = SignInMainFragmentDirections.toSignInPassword(viewModel.email.value.orEmpty())
        navController.navigate(action)
    }

    override fun onSignUpButtonClick(view: View) {
        Log.i(TAG, "user tapped sign up button")

        val action = SignInMainFragmentDirections.toSignUp()
        navController.navigate(action)
    }

    companion object {
        private val TAG = SignInMainFragment::class.java.simpleName
    }
}