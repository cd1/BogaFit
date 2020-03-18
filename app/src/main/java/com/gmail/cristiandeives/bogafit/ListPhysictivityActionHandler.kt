package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

interface ListPhysictivityActionHandler {
    @UiThread
    fun onAddPhysictivityButtonClick(view: View)
}