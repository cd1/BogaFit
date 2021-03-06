package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

@UiThread
interface ProfileActionHandler {
    fun onDisplayNameTextClick(view: View)
    fun onPhoneNumberTextClick(view: View)
    fun onBirthDateTextClick(view: View)
    fun onGenderTextClick(view: View)
    fun onSignOutButtonClick(view: View)
}