package com.gmail.cristiandeives.bogafit

import android.view.View
import androidx.annotation.UiThread

interface MeasurementsActionHandler {
    @UiThread
    fun onWeightTextClick(view: View)
    @UiThread
    fun onHeightTextClick(view: View)
}