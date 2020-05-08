package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.Locale

@MainThread
class ProfileViewModel(app: Application) : AndroidViewModel(app),
    DefaultLifecycleObserver {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser ?: throw IllegalStateException("there is no authenticated user")

    private val _formattedDisplayName = MutableLiveData<String>()
    val formattedDisplayName: LiveData<String> = _formattedDisplayName
    var displayName = ""
        private set(value) {
            _formattedDisplayName.value = formatDisplayName(value)
            field = value
        }

    private val _updateDisplayNameStatus = MutableLiveData<Resource<*>>()
    val updateDisplayNameStatus: LiveData<Resource<*>> = _updateDisplayNameStatus

    private val _formattedPhoneNumber = MutableLiveData<String>()
    val formattedPhoneNumber: LiveData<String> = _formattedPhoneNumber
    var phoneNumber = ""
        private set(value) {
            _formattedPhoneNumber.value = formatPhoneNumber(value)
            field = value
        }

    override fun onStart(owner: LifecycleOwner) {
        Log.v(TAG, "> onStart(...)")

        overrideUiDataFromFirebase()

        Log.d(TAG, "reloading user data...")
        user.reload().addOnSuccessListener {
            Log.d(TAG, "reload user data success")

            overrideUiDataFromFirebase()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "reload user data failed [${ex.message}]", ex)
        }.addOnCanceledListener {
            Log.d(TAG, "reload user data canceled")
        }

        Log.v(TAG, "< onStart(...)")
    }

    @UiThread
    fun updateDisplayName(newName: String) {
        val actualNewName = displayNameValue(newName)

        if (user.displayName == actualNewName) {
            Log.d(TAG, "user display name didn't change; skip update")
            return
        }

        _updateDisplayNameStatus.value = Resource.Loading<Any>()

        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(actualNewName)
            .build()

        Log.d(TAG, "updating user display name to $actualNewName...")
        user.updateProfile(profile).addOnSuccessListener {
            Log.d(TAG, "update user display name success")

            displayName = actualNewName
            _updateDisplayNameStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "update user display name failed [${ex.message}]", ex)
            _updateDisplayNameStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "update user display name canceled")
            _updateDisplayNameStatus.value = Resource.Canceled<Any>()
        }
    }

    @UiThread
    private fun displayNameValue(newName: String) =
        newName.trim().take(DISPLAY_NAME_MAX_LENGTH)

    @UiThread
    private fun formatDisplayName(newName: String) =
        displayNameValue(newName).takeIf { it.isNotEmpty() } ?: getApplication<Application>().getString(R.string.empty_string_value)

    @UiThread
    private fun phoneNumberValue(newNumber: String) =
        newNumber.trim().take(SignInPhoneNumberViewModel.PHONE_NUMBER_MAX_LENGTH)

    @UiThread
    private fun formatPhoneNumber(number: String) = phoneNumberValue(number).takeIf { it.isNotEmpty() }?.let { n ->
        PhoneNumberUtils.formatNumber(n, Locale.getDefault().country)
    } ?: getApplication<Application>().getString(R.string.empty_string_value)

    @UiThread
    private fun overrideUiDataFromFirebase() {
        displayName = user.displayName.orEmpty()
        phoneNumber = user.phoneNumber.orEmpty()
    }

    @UiThread
    fun signOut() {
        Log.d(TAG, "signing out...")

        auth.signOut()
    }

    companion object {
        private val TAG = ProfileViewModel::class.java.simpleName

        const val DISPLAY_NAME_MAX_LENGTH = 50
    }
}