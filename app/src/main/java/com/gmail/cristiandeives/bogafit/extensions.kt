package com.gmail.cristiandeives.bogafit

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import com.google.android.material.snackbar.Snackbar

@UiThread
fun View.hideKeyboard() {
    context.getSystemService(InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(windowToken, 0)
}

@UiThread
fun View.showMessage(@StringRes messageRes: Int) {
    Snackbar.make(this, messageRes, Snackbar.LENGTH_LONG).show()
}