package com.gmail.cristiandeives.bogafit

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@MainThread
class SignInPhoneNumberViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val phoneAuth = PhoneAuthProvider.getInstance()

    val phoneNumber = MutableLiveData<String>()

    var phoneVerificationId: String? = null
        private set

    @UiThread
    private fun phoneNumberValue(number: String = phoneNumber.value.orEmpty()) = number.trim()

    val canVerifyPhoneNumber: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val phoneNumberObserver = Observer<String> { number ->
            value = phoneNumberValue(number).isNotEmpty()
        }

        addSource(phoneNumber, phoneNumberObserver)
    }

    private val _verifyPhoneStatus = MutableLiveData<Resource<Event<String>>>()
    val verifyPhoneStatus: LiveData<Resource<Event<String>>> = _verifyPhoneStatus

    private val phoneVerificationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.v(TAG, "> onCodeSent(...)")

            phoneVerificationId = verificationId
            _verifyPhoneStatus.value = Resource.Success(Event(phoneNumberValue()))

            Log.v(TAG, "< onCodeSent(...)")
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.v(TAG, "> onVerificationCompleted(...)")

            Log.d(TAG, "phone verification success")
            _verifyPhoneStatus.value = Resource.Success(Event(phoneNumberValue()))

            signInOrUpdatePhone(_signInStatus, credential)

            Log.v(TAG, "< onVerificationCompleted(...)")
        }

        override fun onVerificationFailed(ex: FirebaseException) {
            Log.v(TAG, "> onVerificationFailed(...)")

            Log.w(TAG, "phone verification failed [${ex.message}]", ex)
            _verifyPhoneStatus.value = Resource.Error()

            Log.v(TAG, "< onVerificationFailed(...)")
        }
    }

    @UiThread
    fun verifyPhoneNumber(activity: Activity) {
        val actualPhone = phoneNumberValue()

        Log.d(TAG, "verifying phone number [$actualPhone]...")
        _verifyPhoneStatus.value = Resource.Loading()

        auth.useAppLanguage()
        phoneAuth.verifyPhoneNumber(actualPhone, VERIFY_CODE_TIMEOUT_VALUE, VERIFY_CODE_TIMEOUT_UNIT, activity, phoneVerificationCallback)
    }

    private val _signInStatus = MutableLiveData<Resource<SignInReason>>()
    val signInStatus: LiveData<Resource<SignInReason>> = _signInStatus

    @UiThread
    private fun hasPhoneAuthProvider() =
        auth.currentUser?.providerData?.find { it.providerId == PhoneAuthProvider.PROVIDER_ID } != null

    val removePhoneNumberButtonVisibility =
        if (hasPhoneAuthProvider()) View.VISIBLE else View.GONE

    @UiThread
    fun canRemovePhoneNumber() = auth.currentUser?.providerData?.any { userInfo ->
        userInfo.providerId !in arrayOf(FirebaseAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
    } != true

    private val _removePhoneNumberStatus = MutableLiveData<Resource<*>>()
    val removePhoneNumberStatus: LiveData<Resource<*>> = _removePhoneNumberStatus

    @UiThread
    fun removePhoneNumber() {
        _removePhoneNumberStatus.value = Resource.Loading<Any>()

        val user = auth.currentUser ?: throw IllegalStateException("there is no authenticated user")

        Log.d(TAG, "removing phone provider from current user...")
        user.unlink(PhoneAuthProvider.PROVIDER_ID).addOnSuccessListener {
            Log.d(TAG, "remove phone provider success")
            _removePhoneNumberStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.d(TAG, "remove phone provider failed [${ex.message}]", ex)
            _removePhoneNumberStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "remove phone provider canceled")
            _removePhoneNumberStatus.value = Resource.Canceled<Any>()
        }
    }

    sealed class SignInError : RuntimeException() {
        class InvalidCredentials : SignInError()
        class ExistingCredentials : SignInError()
        class Server : SignInError()
    }

    enum class SignInReason {
        SIGN_IN,
        UPDATE_PHONE_NUMBER
    }

    companion object {
        private val TAG = SignInPhoneNumberViewModel::class.java.simpleName

        private const val VERIFY_CODE_TIMEOUT_VALUE = 60L
        private val VERIFY_CODE_TIMEOUT_UNIT = TimeUnit.SECONDS

        const val PHONE_NUMBER_MAX_LENGTH = 15

        fun signInOrUpdatePhone(status: MutableLiveData<Resource<SignInReason>>, credential: PhoneAuthCredential) {
            status.value = Resource.Loading()

            val auth = FirebaseAuth.getInstance()
            val existingUser = auth.currentUser
            val authTask = existingUser?.updatePhoneNumber(credential)
                ?: auth.signInWithCredential(credential)

            Log.d(TAG, "signing in with phone number...")
            authTask.addOnSuccessListener {
                Log.d(TAG, "sign in with phone number success")
                val reason = if (existingUser == null) SignInReason.SIGN_IN else SignInReason.UPDATE_PHONE_NUMBER
                status.value = Resource.Success(reason)
            }.addOnFailureListener { ex ->
                Log.w(TAG, "sign in with phone number failed [${ex.message}]", ex)
                val specificError = when (ex) {
                    is FirebaseAuthInvalidCredentialsException -> SignInError.InvalidCredentials()
                    is FirebaseAuthUserCollisionException -> SignInError.ExistingCredentials()
                    else -> SignInError.Server()
                }
                status.value = Resource.Error(specificError)
            }.addOnCanceledListener {
                Log.d(TAG, "sign in / update user phone number canceled")
                status.value = Resource.Canceled()
            }
        }
    }
}