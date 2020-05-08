package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInMainActionHandler {
    fun onEmailNextButtonClick(view: View)
    fun onSignUpButtonClick(view: View)
    fun onSignInWithPhoneButtonClick(view: View)
}