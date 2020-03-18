package com.gmail.cristiandeives.bogafit.data

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.annotation.UiThread
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@BindingConversion
fun formatDateString(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
    return formatter.format(date)
}

@UiThread
@BindingAdapter("physictivityType")
fun Spinner.setPhysictivityType(type: Physictivity.Type) {
    for (index in 0 until adapter.count) {
        if (getItemAtPosition(index) == type) {
            setSelection(index)
            return
        }
    }
}

@UiThread
@InverseBindingAdapter(attribute = "physictivityType")
fun Spinner.getPhysictivityType() = selectedItem as Physictivity.Type

@UiThread
@BindingAdapter("physictivityTypeAttrChanged")
fun Spinner.setphysictivityTypeAttrChanged(listener: InverseBindingListener) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            listener.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            listener.onChange()
        }
    }
}