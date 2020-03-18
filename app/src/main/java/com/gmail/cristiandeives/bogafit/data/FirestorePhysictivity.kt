package com.gmail.cristiandeives.bogafit.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate

data class FirestorePhysictivity(
    @get:DocumentId val id: String = "",
    val date: Timestamp? = null,
    val type: String = "",
    @get:ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toPhysictivity() = Physictivity(
        id,
        date?.toLocalDate() ?: LocalDate.now(),
        Physictivity.Type.valueOf(type),
        updatedAt?.toLocalDateTime()
    )
}

fun Physictivity.toFirestorePhysictivity() = FirestorePhysictivity(
    id,
    date.toFirestoreTimestamp(),
    type.toString(),
    updatedAt?.toFirestoreTimestamp()
)