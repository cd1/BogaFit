package com.gmail.cristiandeives.bogafit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.cristiandeives.bogafit.data.Physictivity

class ViewModelFactory(private val app: Application, private val existingPhysictivity: Physictivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPhysictivityViewModel::class.java)) {
            return EditPhysictivityViewModel(app, existingPhysictivity) as T
        }

        throw IllegalArgumentException("invalid ViewModel class [$modelClass]")
    }
}