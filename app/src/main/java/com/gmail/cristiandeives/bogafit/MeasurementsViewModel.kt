package com.gmail.cristiandeives.bogafit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.toLocalDate
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.Period

@MainThread
class MeasurementsViewModel : ViewModel(),
    DefaultLifecycleObserver {

    private val repo = FirestoreRepository.getInstance()

    private val _weight = MutableLiveData<Double>()
    val weight: LiveData<Double> = _weight

    private val _height = MutableLiveData<Double>()
    val height: LiveData<Double> = _height

    private val _bmi = MediatorLiveData<Double>().apply {
        val weightObserver = Observer<Double> { weight ->
            value = computeBmi(weight = weight)
        }
        addSource(_weight, weightObserver)

        val heightObserver = Observer<Double> { height ->
            value = computeBmi(height = height)
        }
        addSource(_height, heightObserver)
    }
    val bmi: LiveData<Double> = _bmi

    private val _bfp = MediatorLiveData<Double>().apply {
        val bmiObserver = Observer<Double> { bmi ->
            value = computeBfp(bmi)
        }
        addSource(_bmi, bmiObserver)
    }
    val bfp: LiveData<Double> = _bfp

    @UiThread
    private fun computeBmi(weight: Double = _weight.value ?: INVALID_MEASUREMENT, height: Double = _height.value ?: INVALID_MEASUREMENT) = if (weight >= 0 && height >= 0) {
        weight / (height * height)
    } else {
        INVALID_MEASUREMENT
    }

    @UiThread
    private fun computeBfp(bmi: Double = computeBmi(), birthDate: LocalDate? = this.birthDate, gender: Gender? = this.gender) = if (bmi >= 0 && birthDate != null && gender != null) {
        val today = LocalDate.now()
        val age = Period.between(birthDate, today)
        val ageInYears = age.years + (age.months / 12.0) + (age.days / today.lengthOfYear().toDouble())
        val genderValue = if (gender == Gender.MALE) 1 else 0

        ((1.39 * bmi) + (0.16 * ageInYears) - (10.34 * genderValue) - 9) / 100.0
    } else {
        INVALID_MEASUREMENT
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.v(TAG, "> onStart(...)")

        startListeningToUser()
        startListeningToWeight()
        startListeningToHeight()

        Log.v(TAG, "< onStart(...)")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.v(TAG, "> onStop(...)")

        stopListeningToUser()
        stopListeningToWeight()
        stopListeningToHeight()

        Log.v(TAG, "< onStop(...)")
    }

    private var birthDate: LocalDate? = null
    private var gender: Gender? = null

    private val _loadUserDataStatus = MutableLiveData<Resource<*>>()
    val loadUserDataStatus: LiveData<Resource<*>> = _loadUserDataStatus

    private var userListener: ListenerRegistration? = null

    private fun startListeningToUser() {
        _loadUserDataStatus.value = Resource.Loading<Any>()

        userListener = repo.getUser().addSnapshotListener { snap, ex ->
            if (ex != null) {
                Log.w(TAG, "read user failed [${ex.message}]", ex)
                _loadUserDataStatus.value = Resource.Error<Any>()

                return@addSnapshotListener
            }

            Log.d(TAG, "read user success")

            birthDate = snap?.getTimestamp(FirestoreRepository.USER_FIELD_BIRTH_DATE)
                ?.toLocalDate()

            val genderStr = snap?.getString(FirestoreRepository.USER_FIELD_GENDER).orEmpty()
            gender = runCatching {
                Gender.valueOf(genderStr)
            }.getOrNull()

            _bfp.value = computeBfp(computeBmi())

            _loadUserDataStatus.value = Resource.Success<Any>()
        }
    }

    @UiThread
    private fun stopListeningToUser() {
        userListener?.remove()
    }

    private var weightListener: ListenerRegistration? = null

    @UiThread
    private fun startListeningToWeight() {
        weightListener = repo.getWeight().addSnapshotListener { snap, ex ->
            if (ex != null) {
                Log.w(TAG, "reading weight failed: ${ex.message}", ex)
                return@addSnapshotListener
            }

            Log.d(TAG, "reading weight success")
            _weight.value = snap?.getDouble(FirestoreRepository.WEIGHT_FIELD_VALUE) ?: INVALID_MEASUREMENT
        }
    }

    @UiThread
    private fun stopListeningToWeight() {
        weightListener?.remove()
    }

    private var heightListener: ListenerRegistration? = null

    @UiThread
    private fun startListeningToHeight() {
        heightListener = repo.getHeight().addSnapshotListener { snap, ex ->
            if (ex != null) {
                Log.w(TAG, "reading height failed: ${ex.message}", ex)
                return@addSnapshotListener
            }

            Log.d(TAG, "reading height success")
            _height.value = snap?.getDouble(FirestoreRepository.HEIGHT_FIELD_VALUE) ?: INVALID_MEASUREMENT
        }
    }

    @UiThread
    private fun stopListeningToHeight() {
        heightListener?.remove()
    }

    private val _updateWeightStatus = MutableLiveData<Resource<*>>()
    val updateWeightStatus: LiveData<Resource<*>> = _updateWeightStatus

    @UiThread
    fun updateWeight(weight: Double) {
        val currentWeight = _weight.value ?: INVALID_MEASUREMENT
        if (weight.approximatelyEquals(currentWeight)) {
            Log.d(TAG, "user weight didn't change; skip update")
            return
        }

        _updateWeightStatus.value = Resource.Loading<Any>()

        repo.setWeight(weight).addOnSuccessListener {
            Log.d(TAG, "user weight update success")
            _weight.value = weight
            _updateWeightStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "user weight update failed [${ex.message}]", ex)
            _updateWeightStatus.value = Resource.Error<Any>(ex)
        }.addOnCanceledListener {
            Log.d(TAG, "user weight update canceled")
            _updateWeightStatus.value = Resource.Canceled<Any>()
        }
    }

    private val _updateHeightStatus = MutableLiveData<Resource<*>>()
    val updateHeightStatus: LiveData<Resource<*>> = _updateHeightStatus

    @UiThread
    fun updateHeight(height: Double) {
        val currentHeight = _height.value ?: INVALID_MEASUREMENT
        if (height.approximatelyEquals(currentHeight)) {
            Log.d(TAG, "user height didn't change; skip update")
            return
        }

        _updateHeightStatus.value = Resource.Loading<Any>()

        repo.setHeight(height).addOnSuccessListener {
            Log.d(TAG, "user height update success")
            _height.value = height
            _updateHeightStatus.value = Resource.Success<Any>()
        }.addOnFailureListener { ex ->
            Log.w(TAG, "user height update failed [${ex.message}]", ex)
            _updateHeightStatus.value = Resource.Error<Any>(ex)
        }.addOnCanceledListener {
            Log.d(TAG, "user height update canceled")
            _updateHeightStatus.value = Resource.Canceled<Any>()
        }
    }

    companion object {
        private val TAG = MeasurementsViewModel::class.java.simpleName

        private const val INVALID_MEASUREMENT = -1.0
    }
}