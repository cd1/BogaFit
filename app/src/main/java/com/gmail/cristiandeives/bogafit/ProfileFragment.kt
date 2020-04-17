package com.gmail.cristiandeives.bogafit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gmail.cristiandeives.bogafit.databinding.FragmentProfileBinding

@MainThread
class ProfileFragment : Fragment(),
    View.OnFocusChangeListener,
    ProfileActionHandler {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()

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

        binding.nameEdit.onFocusChangeListener = this

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        Log.v(TAG, "> onFocusChange(v=$v, hasFocus=$hasFocus)")

        when (v.id) {
            R.id.name_edit -> {
                if (!hasFocus) {
                    viewModel.saveDisplayName()
                }
            }
        }

        Log.v(TAG, "< onFocusChange(v=$v, hasFocus=$hasFocus)")
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

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
    }
}