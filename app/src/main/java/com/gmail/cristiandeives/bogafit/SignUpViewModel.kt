package com.gmail.cristiandeives.bogafit

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private val _isSignUpReady = MediatorLiveData<Boolean>().apply {
        val signUpFieldObserver = Observer<String> {
            value = (!email.value.isNullOrBlank()) && (!password.value.isNullOrBlank())
        }

        addSource(email, signUpFieldObserver)
        addSource(password, signUpFieldObserver)
    }
    val isSignUpReady: LiveData<Boolean> = _isSignUpReady

    private val _signUpStatus = MutableLiveData<Resource<*>>()
    val signUpStatus: LiveData<Resource<*>> = _signUpStatus

    fun signUp() {
        _signUpStatus.value = Resource.Loading<Any>()

        val actualEmail = email.value?.trim()
        if (actualEmail.isNullOrEmpty()) {
            _signUpStatus.value = Resource.Error<Any>(Error.MissingEmail())
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(actualEmail).matches()) {
            _signUpStatus.value = Resource.Error<Any>(Error.InvalidEmail())
            return
        }

        val actualPassword = password.value?.trim()
        if (actualPassword.isNullOrEmpty()) {
            _signUpStatus.value = Resource.Error<Any>(Error.MissingPassword())
            return
        }

        auth.createUserWithEmailAndPassword(actualEmail, actualPassword).addOnSuccessListener { result ->
            val uid = result.user?.uid.orEmpty()
            Log.d(TAG, "sign up successful (UID=$uid)")
            _signUpStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "sign up failed [${ex.message}]", ex)
            _signUpStatus.value = Resource.Error<Any>(Error.Server())
        }.addOnCanceledListener {
            Log.d(TAG, "sign up was canceled")
            _signUpStatus.value = Resource.Canceled<Any>()
        }
    }

    sealed class Error : RuntimeException() {
        class MissingEmail : Error()
        class InvalidEmail : Error()
        class MissingPassword : Error()
        class Server : Error()
    }

    companion object {
        private val TAG = SignUpViewModel::class.java.simpleName
    }
}