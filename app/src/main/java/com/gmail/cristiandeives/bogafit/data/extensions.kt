package com.gmail.cristiandeives.bogafit.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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

fun DocumentSnapshot.toPhysictivity() = Physictivity(
    id = id,
    date = getTimestamp(FirestoreRepository.PHYSICTIVITY_FIELD_DATE)?.toLocalDate()
        ?: throw IllegalArgumentException("missing ${FirestoreRepository.PHYSICTIVITY_FIELD_DATE} in document snapshot"),
    type = getString(FirestoreRepository.PHYSICTIVITY_FIELD_TYPE)?.let { Physictivity.Type.valueOf(it) }
        ?: throw IllegalArgumentException("missing ${FirestoreRepository.PHYSICTIVITY_FIELD_TYPE} in document snapshot"),
    updatedAt = getTimestamp(FirestoreRepository.PHYSICTIVITY_FIELD_UPDATED_AT, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toLocalDateTime()
         ?: throw IllegalArgumentException("missing ${FirestoreRepository.PHYSICTIVITY_FIELD_UPDATED_AT} in document snapshot")
)