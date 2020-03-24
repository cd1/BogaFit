package com.gmail.cristiandeives.bogafit

import com.gmail.cristiandeives.bogafit.data.Physictivity
import java.time.LocalDate

class AddPhysictivityViewModel : SavePhysictivityViewModel() {
    override fun runSaveTask(date: LocalDate, type: Physictivity.Type) =
        repo.addPhysictivity(date, type)
}