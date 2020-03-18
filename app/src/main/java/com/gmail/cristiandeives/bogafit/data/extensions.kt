package com.gmail.cristiandeives.bogafit.data

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toFirestoreTimestamp() =
    Timestamp(atZone(ZoneId.systemDefault()).toEpochSecond(), nano)

fun Timestamp.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoseconds.toLong()), ZoneId.systemDefault())

fun LocalDate.toFirestoreTimestamp() =
    atStartOfDay().toFirestoreTimestamp()

fun Timestamp.toLocalDate(): LocalDate =
    toLocalDateTime().toLocalDate()