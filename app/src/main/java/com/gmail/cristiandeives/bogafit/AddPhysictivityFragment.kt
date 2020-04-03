package com.gmail.cristiandeives.bogafit

import androidx.fragment.app.viewModels

class AddPhysictivityFragment : SavePhysictivityFragment() {
    override val viewModel by viewModels<AddPhysictivityViewModel>()
}