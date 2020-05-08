package com.gmail.cristiandeives.bogafit

import android.util.Patterns
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

@MainThread
class SignInViewModel : ViewModel() {
    var email = MutableLiveData<String>()

    val canEnterPassword: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val emailObserver = Observer<String> { newEmail ->
            value = try {
                validateEmail(newEmail)
                true
            } catch (ex: Exception) {
                false
            }
        }

        addSource(email, emailObserver)
    }

    sealed class Error : RuntimeException() {
        class Server : Error()
    }

    companion object {
        const val EMAIL_MAX_LENGTH = 320

        @UiThread
        fun validateEmail(email: String): String {
            val actualEmail = email.trim()
            if (actualEmail.isEmpty()) {
                throw IllegalArgumentException("empty e-mail")
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(actualEmail).matches()) {
                throw IllegalArgumentException("invalid e-mail")
            }

            return actualEmail
        }
    }
}