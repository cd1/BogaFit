package com.gmail.cristiandeives.bogafit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPhysictivityViewModel::class.java)) {
            return EditPhysictivityViewModel(app) as T
        }

        throw IllegalArgumentException("invalid ViewModel class [$modelClass]")
    }
}