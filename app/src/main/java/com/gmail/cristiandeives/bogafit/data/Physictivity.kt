package com.gmail.cristiandeives.bogafit.data

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate
import java.time.LocalDateTime

data class Physictivity(
    val id: String = "",
    val date: LocalDate,
    val type: Type?,
    val updatedAt: LocalDateTime? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        LocalDate.of(parcel.readInt(), parcel.readInt(), parcel.readInt()),
        Type.valueOf(parcel.readString().orEmpty()),
        LocalDateTime.of(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
    )

    enum class Type {
        WEIGHT_LIFTING,
        RUNNING,
        TREADMILL_RUNNING,
        WALKING,
        BIKING
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(date.year)
        parcel.writeInt(date.monthValue)
        parcel.writeInt(date.dayOfMonth)
        parcel.writeString(type.toString())
        parcel.writeInt(updatedAt?.year ?: -1)
        parcel.writeInt(updatedAt?.monthValue ?: -1)
        parcel.writeInt(updatedAt?.dayOfMonth ?: -1)
        parcel.writeInt(updatedAt?.hour ?: -1)
        parcel.writeInt(updatedAt?.minute ?: -1)
        parcel.writeInt(updatedAt?.second ?: -1)
        parcel.writeInt(updatedAt?.nano ?: -1)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Physictivity> {
            override fun createFromParcel(parcel: Parcel) = Physictivity(parcel)

            override fun newArray(size: Int) = arrayOfNulls<Physictivity>(size)
        }
    }
}