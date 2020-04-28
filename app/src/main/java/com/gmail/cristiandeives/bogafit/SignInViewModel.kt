package com.gmail.cristiandeives.bogafit

import android.app.Activity
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.util.Patterns
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

@MainThread
class SignInViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val phoneAuth = PhoneAuthProvider.getInstance()

    var phoneNumber = MutableLiveData<String>()
    var phoneCode = MutableLiveData<String>()
    var email = MutableLiveData<String>()

    private var phoneVerificationId: String? = null

    var formattedPhoneNumber: LiveData<String> = MediatorLiveData<String>().apply {
        val phoneNumberObserver = Observer<String> { number ->
            value = PhoneNumberUtils.formatNumber(phoneNumberValue(number), Locale.getDefault().country)
        }

        addSource(phoneNumber, phoneNumberObserver)
    }

    val canContinueWithPhone: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val phoneNumberObserver = Observer<String> { number ->
            value = phoneNumberValue(number).isNotEmpty()
        }

        addSource(phoneNumber, phoneNumberObserver)
    }

    private val _signInStatus = MutableLiveData<Resource<*>>()
    val signInStatus: LiveData<Resource<*>> = _signInStatus

    private val _phoneVerificationStatus = MutableLiveData<Event<Resource<*>>>()
    val phoneVerificationStatus: LiveData<Event<Resource<*>>> = _phoneVerificationStatus

    val canCheckPhoneVerificationCode: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val statusObserver = Observer<Event<Resource<*>>> { status ->
            value = phoneCodeValue().length == PHONE_CODE_MAX_LENGTH
                    && (status.peek() is Resource.Success)
        }

        val phoneCodeObserver = Observer<String> { code ->
            value = (phoneCodeValue(code).length == PHONE_CODE_MAX_LENGTH)
                    && phoneVerificationStatus.value?.peek() is Resource.Success
        }

        addSource(phoneVerificationStatus, statusObserver)
        addSource(phoneCode, phoneCodeObserver)
    }

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

    @UiThread
    private fun phoneNumberValue(number: String = phoneNumber.value.orEmpty()) = number.trim()

    @UiThread
    private fun phoneCodeValue(code: String = phoneCode.value.orEmpty()) = code.trim()

    private val phoneVerificationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.v(TAG, "> onCodeSent(...)")

            phoneVerificationId = verificationId
            _phoneVerificationStatus.value = Event(Resource.Success<Any>())

            Log.v(TAG, "< onCodeSent(...)")
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.v(TAG, "> onVerificationCompleted(...)")

            Log.d(TAG, "phone verification success")
            doSignIn(credential)

            Log.v(TAG, "< onVerificationCompleted(...)")
        }

        override fun onVerificationFailed(ex: FirebaseException) {
            Log.v(TAG, "> onVerificationFailed(...)")

            Log.w(TAG, "phone verification failed [${ex.message}]", ex)
            _phoneVerificationStatus.value = Event(Resource.Error<Any>())

            Log.v(TAG, "< onVerificationFailed(...)")
        }
    }

    @UiThread
    fun verifyPhoneNumber(activity: Activity) {
        val actualPhone = phoneNumberValue()

        Log.d(TAG, "verifying phone number [$actualPhone]...")
        _phoneVerificationStatus.value = Event(Resource.Loading<Any>())

        auth.useAppLanguage()
        phoneAuth.verifyPhoneNumber(actualPhone, 1, TimeUnit.MINUTES, activity, phoneVerificationCallback)
    }

    @UiThread
    fun checkPhoneVerificationCode() {
        _signInStatus.value = Resource.Loading<Any>()

        val credential = PhoneAuthProvider.getCredential(phoneVerificationId.orEmpty(), phoneCodeValue())
        doSignIn(credential)
    }

    @UiThread
    private fun doSignIn(credential: AuthCredential) {
        auth.signInWithCredential(credential).addOnSuccessListener { result ->
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
        class InvalidCredentials : Error()
        class Server : Error()
    }

    companion object {
        private val TAG = SignInViewModel::class.java.simpleName

        const val EMAIL_MAX_LENGTH = 320
        const val PHONE_CODE_MAX_LENGTH = 6

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