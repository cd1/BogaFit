package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

interface AddPhysictivityActionHandler {
    @UiThread
    fun onDateSelectButtonClick(view: View)
    @UiThread
    fun onAddPhysictivityButtonClick(view: View)
}