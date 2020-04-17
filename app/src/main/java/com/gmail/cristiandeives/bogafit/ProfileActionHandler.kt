package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface ProfileActionHandler {
    fun onSignOutButtonClick(view: View)
}