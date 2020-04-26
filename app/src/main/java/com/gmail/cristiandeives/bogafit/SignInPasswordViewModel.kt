package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@MainThread
class SignInPasswordViewModel(val email: String) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var password = MutableLiveData<String>()

    private val _passwordResetStatus = MutableLiveData<Resource<*>>()
    val passwordResetStatus: LiveData<Resource<*>> = _passwordResetStatus

    @UiThread
    fun sendPasswordResetEmail() {
        _passwordResetStatus.value = Resource.Loading<Any>()

        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            Log.d(TAG, "password reset success")
            _passwordResetStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "password reset failed: ${ex.message}", ex)
            val specificError = when (ex) {
                is FirebaseAuthInvalidUserException -> ResetPasswordError.InvalidEmail()
                else -> ResetPasswordError.Server()
            }
            _passwordResetStatus.value = Resource.Error<Any>(specificError)
        }.addOnCanceledListener {
            Log.d(TAG, "password reset canceled")
            _passwordResetStatus.value = Resource.Canceled<Any>()
        }
    }

    val canSignIn: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val passwordObserver = Observer<String> { newPassword ->
            value = try {
                validatePassword(newPassword)
                true
            } catch (ex: Exception) {
                false
            }
        }

        addSource(password, passwordObserver)
    }

    private val _signInStatus = MutableLiveData<Resource<*>>()
    val signInStatus: LiveData<Resource<*>> = _signInStatus

    @UiThread
    fun signIn() {
        _signInStatus.value = Resource.Loading<Any>()

        val actualPassword = try {
            validatePassword(password.value.orEmpty())
        } catch (ex: Exception) {
            _signInStatus.value = Resource.Error<Any>(SignInError.MissingPassword())
            return
        }

        val credential = EmailAuthProvider.getCredential(email, actualPassword)
        auth.signInWithCredential(credential).addOnSuccessListener { result ->
            val uid = result.user?.uid.orEmpty()
            Log.d(TAG, "sign in successful (UID=$uid)")
            _signInStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "sign in failed [${ex.message}]", ex)
            val specificError = when (ex) {
                is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> SignInError.InvalidCredentials()
                else -> SignInError.Server()
            }
            _signInStatus.value = Resource.Error<Any>(specificError)
        }.addOnCanceledListener {
            Log.d(TAG, "sign in was canceled")
            _signInStatus.value = Resource.Canceled<Any>()
        }
    }

    sealed class SignInError : RuntimeException() {
        class MissingPassword : SignInError()
        class InvalidCredentials : SignInError()
        class Server : SignInError()
    }

    sealed class ResetPasswordError : RuntimeException() {
        class InvalidEmail : ResetPasswordError()
        class Server : ResetPasswordError()
    }

    companion object {
        private val TAG = SignInPasswordViewModel::class.java.simpleName

        @UiThread
        fun validatePassword(password: String): String {
            val actualPassword = password.trim()
            if (actualPassword.isEmpty()) {
                throw IllegalArgumentException("empty password")
            }

            return actualPassword
        }
    }
}