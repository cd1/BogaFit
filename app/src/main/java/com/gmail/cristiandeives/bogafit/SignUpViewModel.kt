package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.UiThread
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

    private val _canSignUp = MediatorLiveData<Boolean>().apply {
        val signUpFieldObserver = Observer<String> {
            value = try {
                SignInViewModel.validateEmail(email.value.orEmpty())
                SignInPasswordViewModel.validatePassword(password.value.orEmpty())
                true
            } catch (ex: Exception) {
                false
            }
        }

        addSource(email, signUpFieldObserver)
        addSource(password, signUpFieldObserver)
    }
    val canSignUp: LiveData<Boolean> = _canSignUp

    private val _signUpStatus = MutableLiveData<Resource<*>>()
    val signUpStatus: LiveData<Resource<*>> = _signUpStatus

    @UiThread
    fun signUp() {
        _signUpStatus.value = Resource.Loading<Any>()

        val actualEmail = try {
            SignInViewModel.validateEmail(email.value.orEmpty())
        } catch (ex: Exception) {
            _signUpStatus.value = Resource.Error<Any>(ex)
            return
        }

        val actualPassword = try {
            SignInPasswordViewModel.validatePassword(password.value.orEmpty())
        } catch (ex: Error) {
            _signUpStatus.value = Resource.Error<Any>(ex)
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