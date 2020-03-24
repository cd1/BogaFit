package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.gmail.cristiandeives.bogafit.data.Physictivity
import java.time.LocalDate

@MainThread
class EditPhysictivityViewModel : SavePhysictivityViewModel() {
    private var id: String = ""

    override fun runSaveTask(date: LocalDate, type: Physictivity.Type) =
        repo.editPhysictivity(id, date, type)

    @UiThread
    fun syncWith(physictivity: Physictivity) {
        Log.d(TAG, "syncing viewmodel data with existing physictivity [$physictivity]")

        id = physictivity.id
        date.value = physictivity.date
        type.value = physictivity.type
    }

    companion object {
        private val TAG = EditPhysictivityViewModel::class.java.simpleName
    }
}