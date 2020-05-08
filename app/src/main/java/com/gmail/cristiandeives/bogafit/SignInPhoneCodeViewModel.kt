package com.gmail.cristiandeives.bogafit

import android.telephony.PhoneNumberUtils
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale

@MainThread
class SignInPhoneCodeViewModel(phoneNumber: String) : ViewModel() {
    val phoneNumber: String = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)

    val phoneCode = MutableLiveData<String>()

    @UiThread
    private fun phoneCodeValue(code: String = phoneCode.value.orEmpty()) = code.trim()

    val canValidatePhoneVerificationCode: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val phoneCodeObserver = Observer<String> { code ->
            value = (phoneCodeValue(code).length == PHONE_CODE_MAX_LENGTH)
        }

        addSource(phoneCode, phoneCodeObserver)
    }

    private val _verifyPhoneCodeStatus = MutableLiveData<Resource<SignInPhoneNumberViewModel.SignInReason>>()
    val verifyPhoneCodeStatus: LiveData<Resource<SignInPhoneNumberViewModel.SignInReason>> = _verifyPhoneCodeStatus

    @UiThread
    fun checkPhoneVerificationCode(phoneVerificationId: String) {
        val credential = PhoneAuthProvider.getCredential(phoneVerificationId, phoneCodeValue())
        SignInPhoneNumberViewModel.signInOrUpdatePhone(_verifyPhoneCodeStatus, credential)
    }

    companion object {
        const val PHONE_CODE_MAX_LENGTH = 6
    }
}