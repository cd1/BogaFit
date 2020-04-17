package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInActionHandler {
    fun onSignInButtonClick(view: View)
    fun onSignUpButtonClick(view: View)
}