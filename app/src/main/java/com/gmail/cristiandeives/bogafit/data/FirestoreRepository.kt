package com.gmail.cristiandeives.bogafit.data

import android.util.Log
import com.gmail.cristiandeives.bogafit.Gender
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addPhysictivity(date: LocalDate, type: Physictivity.Type): Task<DocumentReference> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        val data = mapOf(
            PHYSICTIVITY_UID to uid,
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
        return db.collection(PHYSICTIVITY_COLLECTION).document(id).update(data)
    }

    fun getPhysictivitiesQuery(): Query {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        Log.d(TAG, "querying documents from collection $PHYSICTIVITY_COLLECTION with UID=$uid")
        return db.collection(PHYSICTIVITY_COLLECTION)
            .whereEqualTo(PHYSICTIVITY_UID, uid)
            .orderBy(PHYSICTIVITY_FIELD_DATE, Query.Direction.DESCENDING)
    }

    fun deletePhysictivity(id: String): Task<*> {
        Log.d(TAG, "deleting document [id=$id] from collection $PHYSICTIVITY_COLLECTION...")
        return db.collection(PHYSICTIVITY_COLLECTION).document(id).delete()
    }

    fun setWeight(weight: Double): Task<*> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        val data = mapOf(
            WEIGHT_FIELD_VALUE to weight,
        )

        Log.d(TAG, "setting document [id=$uid, $data] to collection $WEIGHT_COLLECTION...")
        return db.collection(WEIGHT_COLLECTION).document(uid).set(data)
    }

    fun getWeight(): DocumentReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        Log.d(TAG, "reading document [id=$uid] from collection $WEIGHT_COLLECTION")
        return db.collection(WEIGHT_COLLECTION).document(uid)
    }

    fun setHeight(height: Double): Task<*> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        val data = mapOf(
            HEIGHT_FIELD_VALUE to height,
        )

        Log.d(TAG, "setting document [id=$uid, $data] to collection $HEIGHT_COLLECTION...")
        return db.collection(HEIGHT_COLLECTION).document(uid).set(data)
    }

    fun getHeight(): DocumentReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        Log.d(TAG, "reading document [id=$uid] from collection $HEIGHT_COLLECTION")
        return db.collection(HEIGHT_COLLECTION).document(uid)
    }

    fun setBirthDate(date: LocalDate) = setUserData(mapOf(
        USER_FIELD_BIRTH_DATE to date.toFirestoreTimestamp(),
    ))

    fun setGender(gender: Gender) = setUserData(mapOf(
        USER_FIELD_GENDER to gender.name,
    ))

    private fun setUserData(data: Map<String, Any>): Task<*> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        val collection = USER_COLLECTION
        Log.d(TAG, "updating document [id=$uid, $data] in collection $collection...")
        return db.collection(collection).document(uid).set(data, SetOptions.merge())
    }

    fun getUser(): DocumentReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("there is no authenticated user")

        val collection = USER_COLLECTION
        Log.d(TAG, "reading document [id=$uid] from collection $collection")
        return db.collection(collection).document(uid)
    }

    companion object {
        private const val PHYSICTIVITY_COLLECTION = "physictivity"
        const val PHYSICTIVITY_UID = "uid"
        const val PHYSICTIVITY_FIELD_DATE = "date"
        const val PHYSICTIVITY_FIELD_TYPE = "type"
        const val PHYSICTIVITY_FIELD_UPDATED_AT = "updatedAt"

        private const val WEIGHT_COLLECTION = "weight"
        const val WEIGHT_FIELD_VALUE = "weight"

        private const val HEIGHT_COLLECTION = "height"
        const val HEIGHT_FIELD_VALUE = "height"

        private const val USER_COLLECTION = "user"
        const val USER_FIELD_BIRTH_DATE = "birthDate"
        const val USER_FIELD_GENDER = "gender"

        private val TAG = FirestoreRepository::class.java.simpleName

        private var instance: FirestoreRepository? = null

        fun getInstance() = instance ?: FirestoreRepository().also { instance = it }
    }
}