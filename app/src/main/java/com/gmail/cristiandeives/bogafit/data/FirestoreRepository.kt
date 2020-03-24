package com.gmail.cristiandeives.bogafit.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addPhysictivity(date: LocalDate, type: Physictivity.Type): Task<DocumentReference> {
        val data = mapOf(
            PHYSICTIVITY_FIELD_DATE to date.toFirestoreTimestamp(),
            PHYSICTIVITY_FIELD_TYPE to type,
            PHYSICTIVITY_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )

        Log.d(TAG, "adding document [$data] to collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).add(data)
    }

    fun editPhysictivity(id: String, date: LocalDate, type: Physictivity.Type): Task<*> {
        val data = mapOf(
            PHYSICTIVITY_FIELD_DATE to date.toFirestoreTimestamp(),
            PHYSICTIVITY_FIELD_TYPE to type,
            PHYSICTIVITY_FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )

        Log.d(TAG, "editing document [id=${id}, $data] in collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).document(id).set(data)
    }

    fun getPhysictivitiesQuery(): Query {
        Log.d(TAG, "querying documents from collection $PHYSICTIVITY_COLLECTION")
        return db.collection(PHYSICTIVITY_COLLECTION)
            .orderBy(PHYSICTIVITY_FIELD_DATE, Query.Direction.DESCENDING)
    }

    fun deletePhysictivity(id: String): Task<*> {
        Log.d(TAG, "deleting document [id=$id] from collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).document(id).delete()
    }

    companion object {
        private const val PHYSICTIVITY_COLLECTION = "physictivity"
        const val PHYSICTIVITY_FIELD_DATE = "date"
        const val PHYSICTIVITY_FIELD_TYPE = "type"
        const val PHYSICTIVITY_FIELD_UPDATED_AT = "updatedAt"

        private val TAG = FirestoreRepository::class.java.simpleName

        private var instance: FirestoreRepository? = null

        fun getInstance() = instance ?: FirestoreRepository().also { instance = it }
    }
}