package com.gmail.cristiandeives.bogafit

import android.util.Log
import android.util.Patterns
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@MainThread
class SignInViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private val _isSignInReady = MediatorLiveData<Boolean>().apply {
        val signInFieldObserver = Observer<String> {
            value = (!email.value.isNullOrBlank()) && (!password.value.isNullOrBlank())
        }

        addSource(email, signInFieldObserver)
        addSource(password, signInFieldObserver)
    }
    val isSignInReady: LiveData<Boolean> = _isSignInReady

    private val _signInStatus = MutableLiveData<Resource<*>>()
    val signInStatus: LiveData<Resource<*>> = _signInStatus

    @UiThread
    fun signIn() {
        _signInStatus.value = Resource.Loading<Any>()

        val actualEmail = email.value?.trim()
        if (actualEmail.isNullOrEmpty()) {
            _signInStatus.value = Resource.Error<Any>(Error.MissingEmail())
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(actualEmail).matches()) {
            _signInStatus.value = Resource.Error<Any>(Error.InvalidEmail())
            return
        }

        val actualPassword = password.value?.trim()
        if (actualPassword.isNullOrEmpty()) {
            _signInStatus.value = Resource.Error<Any>(Error.MissingPassword())
            return
        }

        auth.signInWithEmailAndPassword(actualEmail, actualPassword).addOnSuccessListener { result ->
            val uid = result.user?.uid.orEmpty()
            Log.d(TAG, "sign in successful (UID=$uid)")
            _signInStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "sign in failed [${ex.message}]", ex)
            val specificError = when (ex) {
                is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> Error.InvalidCredentials()
                else -> Error.Server()
            }
            _signInStatus.value = Resource.Error<Any>(specificError)
        }.addOnCanceledListener {
            Log.d(TAG, "sign in was canceled")
            _signInStatus.value = Resource.Canceled<Any>()
        }
    }

    sealed class Error : RuntimeException() {
        class MissingEmail : Error()
        class InvalidEmail : Error()
        class MissingPassword : Error()
        class InvalidCredentials : Error()
        class Server : Error()
    }

    companion object {
        private val TAG = SignInViewModel::class.java.simpleName
    }
}