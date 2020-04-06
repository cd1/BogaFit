package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toPhysictivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

@MainThread
class ListPhysictivityViewModel : ViewModel() {
    private val repo = FirestoreRepository.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _listPhysictivitiesStatus = MutableLiveData<Resource<List<Physictivity>>>()
    val listPhysictivityStatus = _listPhysictivitiesStatus

    private val _currentUserStatus = MutableLiveData<Resource<*>>().apply {
        value = auth.currentUser?.let { Resource.Success<Any>() }
    }
    val currentUserStatus: LiveData<Resource<*>> = _currentUserStatus

    init {
        if (_currentUserStatus.value == null) {
            signInAnonymous()
        }
    }

    @UiThread
    private fun signInAnonymous() {
        Log.d(TAG, "signing in with an anonymous account...")
        _currentUserStatus.value = Resource.Loading<Any>()

        auth.signInAnonymously().addOnSuccessListener { result ->
            val uid = result.user?.uid.orEmpty()
            Log.d(TAG, "user is now signed in (UID=$uid)")
            _currentUserStatus.value = Resource.Success<Any>()
        }.addOnFailureListener {  ex ->
            Log.w(TAG, "anonymous sign in failed: ${ex.message}", ex)
            _currentUserStatus.value = Resource.Error<Any>(ex)
        }.addOnCanceledListener {
            Log.d(TAG, "anonymous sign in canceled")
            _currentUserStatus.value = Resource.Canceled<Any>()
        }
    }

    @UiThread
    fun listenToPhysictivities() {
        if (_listPhysictivitiesStatus.value != null) {
            return
        }

        _listPhysictivitiesStatus.value = Resource.Loading()

        repo.getPhysictivitiesQuery().addSnapshotListener { snap, ex ->
            if (ex != null) {
                Log.w(TAG, "read physictivities failed [${ex.message})", ex)
                _listPhysictivitiesStatus.value = Resource.Error(Error.Server())
                return@addSnapshotListener
            }

            Log.d(TAG, "read physictivities success (${snap?.size() ?: 0} elements)")

            if (snap?.isEmpty != false) {
                _listPhysictivitiesStatus.value = Resource.Success(emptyList())
            } else {
                val physictivities = snap.documents.map(DocumentSnapshot::toPhysictivity)
                _listPhysictivitiesStatus.value = Resource.Success(physictivities)
            }
        }
    }

    sealed class Error : RuntimeException() {
        class Server : Error()
    }

    companion object {
        private val TAG = ListPhysictivityViewModel::class.java.simpleName
    }
}