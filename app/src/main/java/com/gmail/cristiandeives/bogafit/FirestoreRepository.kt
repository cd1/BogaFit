package com.gmail.cristiandeives.bogafit

import android.util.Log
import com.gmail.cristiandeives.bogafit.data.FirestorePhysictivity
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toFirestorePhysictivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addPhysictivity(physictivity: Physictivity): Task<DocumentReference> {
        val fsPhysictivity = physictivity.toFirestorePhysictivity()

        Log.d(TAG, "adding document [$fsPhysictivity] to collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).add(fsPhysictivity)
    }

    fun getPhysictivitiesQuery(): Query {
        Log.d(TAG, "querying documents from collection $PHYSICTIVITY_COLLECTION")
        return db.collection(PHYSICTIVITY_COLLECTION)
            .orderBy(PHYSICTIVITY_FIELD_DATE, Query.Direction.DESCENDING)
    }

    companion object {
        private const val PHYSICTIVITY_COLLECTION = "physictivity"
        private val PHYSICTIVITY_FIELD_DATE = FirestorePhysictivity::date.name

        private val TAG = FirestoreRepository::class.java.simpleName

        private var instance: FirestoreRepository? = null

        fun getInstance() = instance ?: FirestoreRepository().also { instance = it }
    }
}