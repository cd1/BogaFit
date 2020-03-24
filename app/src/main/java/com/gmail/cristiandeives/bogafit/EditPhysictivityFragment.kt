package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs

@MainThread
class EditPhysictivityFragment : SavePhysictivityFragment() {
    private val args by navArgs<EditPhysictivityFragmentArgs>()

    override val viewModel by viewModels<EditPhysictivityViewModel>()
    override val saveButtonText = R.string.edit_physictivity_save_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.syncWith(args.physictivity)
    }
}