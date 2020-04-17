package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toPhysictivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

@MainThread
class ListPhysictivityViewModel : ViewModel(),
    DefaultLifecycleObserver {

    private val repo = FirestoreRepository.getInstance()

    private val _listPhysictivitiesStatus = MutableLiveData<Resource<List<Physictivity>>>()
    val listPhysictivityStatus = _listPhysictivitiesStatus

    private var physictivitiesQueryListener: ListenerRegistration? = null

    override fun onCreate(owner: LifecycleOwner) {
        Log.v(TAG, "> onCreate(...)")

        startListeningToPhysictivities()

        Log.v(TAG, "< onCreate(...)")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v(TAG, "> onDestroy(...)")

        stopListeningToPhysictivities()

        Log.v(TAG, "< onDestroy(...)")
    }

    @UiThread
    private fun startListeningToPhysictivities() {
        if (physictivitiesQueryListener != null) {
            Log.d(TAG, "we're already listening to physictivities; don't listen again")
            return
        }

        _listPhysictivitiesStatus.value = Resource.Loading()

        physictivitiesQueryListener = repo.getPhysictivitiesQuery().addSnapshotListener { snap, ex ->
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

    @UiThread
    private fun stopListeningToPhysictivities() {
        physictivitiesQueryListener?.remove()
        physictivitiesQueryListener = null
    }

    sealed class Error : RuntimeException() {
        class Server : Error()
    }

    companion object {
        private val TAG = ListPhysictivityViewModel::class.java.simpleName
    }
}