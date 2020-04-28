package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInPhoneCodeActionHandler {
    fun onVerifyPhoneCodeButtonClick(view: View)
}