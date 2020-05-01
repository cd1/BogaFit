package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@MainThread
class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser ?: throw IllegalStateException("there is no authenticated user")

    private val _formattedDisplayName = MutableLiveData<String>()
    val formattedDisplayName: LiveData<String> = _formattedDisplayName
    var displayName = ""
        private set(value) {
            _formattedDisplayName.value = formatDisplayName(value)
            field = value
        }

    val updateDisplayNameStatus = MutableLiveData<Resource<*>>()

    init {
        displayName = user.displayName.orEmpty()
    }

    @UiThread
    fun updateDisplayName(newName: String) {
        val actualNewName = validateDisplayName(newName)

        if (user.displayName == actualNewName) {
            Log.d(TAG, "user display name didn't change; skip update")
            return
        }

        updateDisplayNameStatus.value = Resource.Loading<Any>()

        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(actualNewName)
            .build()

        Log.d(TAG, "updating user display name to $actualNewName...")
        user.updateProfile(profile).addOnSuccessListener {
            Log.d(TAG, "update user display name success")

            displayName = actualNewName
            updateDisplayNameStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "update user display name failed [${ex.message}]", ex)
            updateDisplayNameStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "update user display name canceled")
            updateDisplayNameStatus.value = Resource.Canceled<Any>()
        }
    }

    @UiThread
    private fun validateDisplayName(newName: String): String {
        val validatedName = newName.trim().take(DISPLAY_NAME_MAX_LENGTH)
        if (validatedName != newName) {
            Log.d(TAG, "after validation, display name is now \"$validatedName\"")
        }
        return validatedName
    }

    @UiThread
    private fun formatDisplayName(newName: String) =
        validateDisplayName(newName).takeIf { it.isNotEmpty() } ?: getApplication<Application>().getString(R.string.empty_string_value)

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