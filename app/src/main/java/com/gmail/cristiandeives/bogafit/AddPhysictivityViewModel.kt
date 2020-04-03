package com.gmail.cristiandeives.bogafit

import android.app.Application
import com.gmail.cristiandeives.bogafit.data.Physictivity
import java.time.LocalDate

class AddPhysictivityViewModel(app: Application) : SavePhysictivityViewModel(app) {
    override val saveButtonText = getApplication<Application>().getString(R.string.add_physictivity_save_button)

    override fun runSaveTask(date: LocalDate, type: Physictivity.Type) =
        repo.addPhysictivity(date, type)
}