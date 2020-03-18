package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.Physictivity
import java.time.LocalDate

@MainThread
class AddPhysictivityViewModel : ViewModel() {
    private val repo = FirestoreRepository.getInstance()
    private val _addStatus = MutableLiveData<Resource<Physictivity>>()

    val date = MutableLiveData<LocalDate>().apply {
        value = LocalDate.now()
    }
    val physictivityType = MutableLiveData<Physictivity.Type>().apply {
        value = Physictivity.Type.WEIGHT_LIFTING
    }

    val addPhysictivityStatus: LiveData<Resource<Physictivity>> = _addStatus

    @UiThread
    fun addPhysictivity() {
        _addStatus.value = Resource.Loading()

        val actualDate = date.value ?: LocalDate.now()

        if (actualDate > LocalDate.now()) {
            Log.d(TAG, "cannot add physictivity: date is in the future [$actualDate]")
            _addStatus.value = Resource.Error(Error.InvalidDate())
            return
        }

        val actualPhysictivityType = physictivityType.value ?: Physictivity.Type.WEIGHT_LIFTING

        val physictivity = Physictivity(
            date = actualDate,
            type = actualPhysictivityType
        )

        repo.addPhysictivity(physictivity).addOnSuccessListener {
            Log.d(TAG, "add physictivity success")
            _addStatus.value = Resource.Success()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "add physictivity failed [${ex.message}]", ex)
            _addStatus.value = Resource.Error(Error.Server())
        }.addOnCanceledListener {
            Log.d(TAG, "add physictivity canceled")
            _addStatus.value = Resource.Canceled()
        }
    }

    sealed class Error : RuntimeException() {
        class InvalidDate : Error()
        class Server : Error()
    }

    companion object {
        private val TAG = AddPhysictivityViewModel::class.simpleName
    }
}