package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInPasswordActionHandler {
    fun onSignInButtonClick(view: View)
    fun onForgotPasswordButtonClick(view: View)
}