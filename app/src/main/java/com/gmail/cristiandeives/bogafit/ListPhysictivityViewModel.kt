package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toPhysictivity
import com.google.firebase.firestore.DocumentSnapshot

@MainThread
class ListPhysictivityViewModel : ViewModel() {
    private val repo = FirestoreRepository.getInstance()

    private val _listPhysictivitiesStatus = MutableLiveData<Resource<List<Physictivity>>>()
    val listPhysictivityStatus = _listPhysictivitiesStatus

    init {
        listenToPhysictivities()
    }

    @UiThread
    private fun listenToPhysictivities() {
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