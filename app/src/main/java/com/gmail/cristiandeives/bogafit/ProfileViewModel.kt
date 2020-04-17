package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@MainThread
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser ?: throw IllegalStateException("there is no authenticated user")

    var displayName = MutableLiveData<String>().apply {
        value = user.displayName
    }

    @UiThread
    fun saveDisplayName() {
        val actualName = displayName.value?.trim().orEmpty()

        if (user.displayName == actualName) {
            Log.d(TAG, "user display name didn't change; skip update")
            return
        }

        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(actualName)
            .build()

        Log.d(TAG, "updating user display name to $actualName...")
        user.updateProfile(profile).addOnSuccessListener {
            Log.d(TAG, "update user display name success")
        }.addOnFailureListener { ex ->
            Log.w(TAG, "update user display name failed [${ex.message}]", ex)
        }.addOnCanceledListener {
            Log.d(TAG, "update user display name canceled")
        }
    }

    @UiThread
    fun signOut() {
        Log.d(TAG, "signing out...")

        auth.signOut()
    }

    sealed class Error : RuntimeException() {
        class Server : Error()
    }

    companion object {
        private val TAG = ProfileViewModel::class.java.simpleName
    }
}