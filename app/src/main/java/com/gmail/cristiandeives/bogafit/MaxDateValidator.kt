package com.gmail.cristiandeives.bogafit

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker

class MaxDateValidator(private val maxDate: Long) : CalendarConstraints.DateValidator {
    override fun isValid(date: Long) =
        date <= maxDate

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(maxDate)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MaxDateValidator> {
            override fun createFromParcel(source: Parcel) = MaxDateValidator(source.readLong())

            override fun newArray(size: Int) = arrayOfNulls<MaxDateValidator>(size)
        }

        fun untilToday() = MaxDateValidator(MaterialDatePicker.todayInUtcMilliseconds())
    }
}