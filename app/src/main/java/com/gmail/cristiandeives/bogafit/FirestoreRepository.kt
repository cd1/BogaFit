package com.gmail.cristiandeives.bogafit

import android.util.Log
import com.gmail.cristiandeives.bogafit.data.FirestorePhysictivity
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.data.toFirestorePhysictivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addPhysictivity(physictivity: Physictivity): Task<DocumentReference> {
        val fsPhysictivity = physictivity.toFirestorePhysictivity()

        Log.d(TAG, "adding document [$fsPhysictivity] to collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).add(fsPhysictivity)
    }

    companion object {
        private const val PHYSICTIVITY_COLLECTION = "physictivity"

        private val TAG = FirestoreRepository::class.java.simpleName

        private var instance: FirestoreRepository? = null

        fun getInstance() = instance ?: FirestoreRepository().also { instance = it }
    }
}