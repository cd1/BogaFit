package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toPhysictivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

@MainThread
class ListPhysictivityViewModel(private val context: Application) : AndroidViewModel(context),
    DefaultLifecycleObserver {

    private val repo = FirestoreRepository.getInstance()
    private lateinit var sharedPref: SharedPreferences

    private val _listPhysictivitiesStatus = MutableLiveData<Resource<List<Physictivity>>>()
    val listPhysictivityStatus: LiveData<Resource<List<Physictivity>>> = _listPhysictivitiesStatus

    private var physictivitiesQueryListener: ListenerRegistration? = null

    val isPhysictivityGoalEnabled: Boolean
        get() = sharedPref.getBoolean(SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_ENABLED,
            context.resources.getBoolean(R.bool.default_physictivity_goal_enabled))

    val physictivityGoal: Int
        get() = sharedPref.getString(SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_VALUE, null)?.toIntOrNull()
            ?: context.resources.getString(R.string.default_physictivity_goal).toInt()

    override fun onCreate(owner: LifecycleOwner) {
        Log.v(TAG, "> onCreate(...)")

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
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