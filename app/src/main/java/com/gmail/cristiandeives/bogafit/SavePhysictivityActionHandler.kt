package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

interface SavePhysictivityActionHandler {
    @UiThread
    fun onDateSelectButtonClick(view: View)
    @UiThread
    fun onSaveButtonClick(view: View)
}