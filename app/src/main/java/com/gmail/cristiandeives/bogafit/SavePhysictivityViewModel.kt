package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.google.android.gms.tasks.Task
import java.time.LocalDate

@MainThread
abstract class SavePhysictivityViewModel(app: Application) : AndroidViewModel(app) {
    internal val repo = FirestoreRepository.getInstance()

    abstract val saveButtonText: String

    private val _savePhysictivityStatus = MutableLiveData<Resource<Physictivity>>()
    val savePhysictivityStatus: LiveData<Resource<Physictivity>> = _savePhysictivityStatus

    val date = MutableLiveData<LocalDate>().apply {
        value = LocalDate.now()
    }
    val type = MutableLiveData<Physictivity.Type>().apply {
        value = Physictivity.Type.WEIGHT_LIFTING
    }

    @UiThread
    fun savePhysictivity() {
        _savePhysictivityStatus.value = Resource.Loading()

        val actualDate = date.value ?: LocalDate.now()

        if (actualDate > LocalDate.now()) {
            Log.d(TAG, "cannot save physictivity: date is in the future [$actualDate]")
            _savePhysictivityStatus.value = Resource.Error(Error.InvalidDate())
            return
        }

        val actualType = type.value ?: Physictivity.Type.WEIGHT_LIFTING

        runSaveTask(actualDate, actualType).addOnSuccessListener {
            Log.d(TAG, "save physictivity success")
            _savePhysictivityStatus.value = Resource.Success()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "save physictivity failed [${ex.message}]", ex)
            _savePhysictivityStatus.value = Resource.Error(Error.Server())
        }.addOnCanceledListener {
            Log.d(TAG, "save physictivity canceled")
            _savePhysictivityStatus.value = Resource.Canceled()
        }
    }

    @UiThread
    abstract fun runSaveTask(date: LocalDate, type: Physictivity.Type): Task<*>

    sealed class Error : RuntimeException() {
        class InvalidDate : Error()
        class Server : Error()
    }

    companion object {
        private val TAG = SavePhysictivityViewModel::class.simpleName
    }
}
