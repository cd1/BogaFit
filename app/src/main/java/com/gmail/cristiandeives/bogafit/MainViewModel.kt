package com.gmail.cristiandeives.bogafit

import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

@MainThread
class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    @UiThread
    fun isUserAuthenticated(auth: FirebaseAuth = this.auth) =
        auth.currentUser != null
}