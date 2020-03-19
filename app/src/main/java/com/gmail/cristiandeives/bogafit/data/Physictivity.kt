package com.gmail.cristiandeives.bogafit.data

import java.time.LocalDate
import java.time.LocalDateTime

data class Physictivity(
    val id: String = "",
    val date: LocalDate,
    val type: Type?,
    val updatedAt: LocalDateTime? = null
) {

    enum class Type {
        WEIGHT_LIFTING,
        RUNNING,
        TREADMILL_RUNNING,
        WALKING,
        BIKING
    }
}