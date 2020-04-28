package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInMainActionHandler {
    fun onPhoneNextButtonClick(view: View)
    fun onEmailNextButtonClick(view: View)
    fun onSignUpButtonClick(view: View)
}