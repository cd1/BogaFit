package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gmail.cristiandeives.bogafit.data.Physictivity
import java.time.LocalDate

@MainThread
class EditPhysictivityViewModel(app: Application, existingPhysictivity: Physictivity) : SavePhysictivityViewModel(app) {
    private var id = existingPhysictivity.id

    private val _deletePhysictivityStatus = MutableLiveData<Resource<Any>>()
    val deletePhysictivityStatus: LiveData<Resource<Any>> = _deletePhysictivityStatus

    override val saveButtonText = getApplication<Application>().getString(R.string.edit_physictivity_save_button)

    init {
        date.value = existingPhysictivity.date
        type.value = existingPhysictivity.type
    }

    override fun runSaveTask(date: LocalDate, type: Physictivity.Type) =
        repo.editPhysictivity(id, date, type)

    @UiThread
    fun deletePhysictivity() {
        _deletePhysictivityStatus.value = Resource.Loading()

        repo.deletePhysictivity(id).addOnSuccessListener {
            Log.d(TAG, "delete physictivity success")
            _deletePhysictivityStatus.value = Resource.Success()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "delete physictivity failed [${ex.message}]", ex)
            _deletePhysictivityStatus.value = Resource.Error(Error.Server())
        }.addOnCanceledListener {
            Log.d(TAG, "delete physictivity canceled")
            _deletePhysictivityStatus.value = Resource.Canceled()
        }
    }

    companion object {
        private val TAG = EditPhysictivityViewModel::class.java.simpleName
    }
}