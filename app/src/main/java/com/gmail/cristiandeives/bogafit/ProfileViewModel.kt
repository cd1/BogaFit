package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.toLocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate

@MainThread
class ProfileViewModel : ViewModel(),
    DefaultLifecycleObserver {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser ?: throw IllegalStateException("there is no authenticated user")

    private val repo = FirestoreRepository.getInstance()

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> = _displayName

    private val _updateDisplayNameStatus = MutableLiveData<Resource<*>>()
    val updateDisplayNameStatus: LiveData<Resource<*>> = _updateDisplayNameStatus

    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> = _phoneNumber

    override fun onStart(owner: LifecycleOwner) {
        Log.v(TAG, "> onStart(...)")

        overrideUiDataFromFirebase()

        reloadUserData()
        startListeningToUser()

        Log.v(TAG, "< onStart(...)")
    }

    override fun onStop(owner: LifecycleOwner) {
        stopListeningToUser()
    }

    private val _loadUserDataStatus = MutableLiveData<Resource<*>>()
    val loadUserDataStatus: LiveData<Resource<*>> = _loadUserDataStatus

    @UiThread
    private fun reloadUserData() {
        Log.d(TAG, "reloading user data...")
        _loadUserDataStatus.value = Resource.Loading<Any>()
        user.reload().addOnSuccessListener {
            Log.d(TAG, "reload user data success")

            overrideUiDataFromFirebase()
            _loadUserDataStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "reload user data failed [${ex.message}]", ex)
            _loadUserDataStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "reload user data canceled")
            _loadUserDataStatus.value = Resource.Canceled<Any>()
        }
    }

    private var userListener: ListenerRegistration? = null

    @UiThread
    private fun startListeningToUser() {
        userListener = repo.getUser().addSnapshotListener { snap, ex ->
            if (ex != null) {
                Log.w(TAG, "read user failed [${ex.message}]", ex)
                return@addSnapshotListener
            }

            Log.d(TAG, "read user success")

            _birthDate.value = snap?.getTimestamp(FirestoreRepository.USER_FIELD_BIRTH_DATE)
                ?.toLocalDate()
            _gender.value = snap?.getString(FirestoreRepository.USER_FIELD_GENDER)?.let { gender ->
                runCatching {
                    Gender.valueOf(gender)
                }.getOrNull()
            }
        }
    }

    @UiThread
    private fun stopListeningToUser() {
        userListener?.remove()
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

            _displayName.value = actualNewName
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

    private val _birthDate = MutableLiveData<LocalDate>()
    val birthDate: LiveData<LocalDate> = _birthDate

    private val _updateBirthDateStatus = MutableLiveData<Resource<*>>()
    val updateBirthDateStatus: LiveData<Resource<*>> = _updateBirthDateStatus

    @UiThread
    fun updateBirthDate(date: LocalDate) {
        if (date == birthDate.value) {
            Log.d(TAG, "user birth date didn't change; skip update")
            return
        }

        _updateBirthDateStatus.value = Resource.Loading<Any>()

        repo.setBirthDate(date).addOnSuccessListener {
            Log.d(TAG, "update birth date success")

            _updateBirthDateStatus.value = Resource.Success<Any>()
            _birthDate.value = date
        }.addOnFailureListener { ex ->
            Log.w(TAG, "update birth date failed [${ex.message}]", ex)
            _updateBirthDateStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "update birth date canceled")
            _updateBirthDateStatus.value = Resource.Canceled<Any>()
        }
    }

    private val _gender = MutableLiveData<Gender>()
    val gender: LiveData<Gender> = _gender

    private val _updateGenderStatus = MutableLiveData<Resource<*>>()
    val updateGenderStatus: LiveData<Resource<*>> = _updateGenderStatus

    fun updateGender(gender: Gender) {
        if (gender == _gender.value) {
            Log.d(TAG, "user gender didn't change; skip update")
            return
        }

        Log.d(TAG, "updating gender...")
        _updateGenderStatus.value = Resource.Loading<Any>()

        repo.setGender(gender).addOnSuccessListener {
            Log.d(TAG, "update gender success")
            _updateGenderStatus.value = Resource.Success<Any>()
            _gender.value =  gender
        }.addOnFailureListener { ex ->
            Log.w(TAG, "update gender failed [${ex.message}]", ex)
            _updateGenderStatus.value = Resource.Error<Any>()
        }.addOnCanceledListener {
            Log.d(TAG, "update gender canceled")
            _updateGenderStatus.value = Resource.Canceled<Any>()
        }
    }

    @UiThread
    private fun overrideUiDataFromFirebase() {
        _displayName.value = user.displayName
        _phoneNumber.value = user.phoneNumber
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