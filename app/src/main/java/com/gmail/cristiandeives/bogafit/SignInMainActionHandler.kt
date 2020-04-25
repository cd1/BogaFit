package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInMainActionHandler {
    fun onNextButtonClick(view: View)
    fun onSignUpButtonClick(view: View)
}