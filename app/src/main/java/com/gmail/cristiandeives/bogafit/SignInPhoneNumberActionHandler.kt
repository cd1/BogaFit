package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface SignInPhoneNumberActionHandler {
    fun onNextButtonClick(view: View)
    fun onRemovePhoneNumberButtonClick(view: View)
}