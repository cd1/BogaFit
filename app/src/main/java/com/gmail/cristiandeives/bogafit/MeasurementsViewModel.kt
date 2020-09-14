package com.gmail.cristiandeives.bogafit

import android.app.Application
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.gmail.cristiandeives.bogafit.data.toLocalDate
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.Period
import java.util.Locale

@MainThread
class MeasurementsViewModel(private val context: Application) : AndroidViewModel(context),
    DefaultLifecycleObserver {

    private val repo = FirestoreRepository.getInstance()

    private lateinit var weightFormatter: MeasureFormat
    private lateinit var heightFormatter: MeasureFormat
    private lateinit var bmiFormatter: NumberFormat
    private lateinit var bfpFormatter: NumberFormat

    var weight = INVALID_MEASUREMENT
        private set(value) {
            field = value

            updateFormattedValues()
        }
    private val _formattedWeight = MutableLiveData<String>()
    val formattedWeight: LiveData<String> = _formattedWeight

    @UiThread
    private fun formatWeight() = if (weight >= 0) {
        weightFormatter.format(Measure(weight, MeasureUnit.KILOGRAM))
    } else {
        context.getString(R.string.empty_string_value)
    }

    var height = INVALID_MEASUREMENT
        private set(value) {
            field = value

            updateFormattedValues()
        }
    private val _formattedHeight = MutableLiveData<String>()
    val formattedHeight: LiveData<String> = _formattedHeight

    @UiThread
    private fun formatHeight() = if (height >= 0) {
        heightFormatter.format(Measure(height, MeasureUnit.METER))
    } else {
        context.getString(R.string.empty_string_value)
    }

    private val _formattedBmi = MediatorLiveData<String>()
    val formattedBmi: LiveData<String> = _formattedBmi

    @UiThread
    private fun computeBmi() = if (weight >= 0 && height >= 0) {
        weight / (height * height)
    } else {
        INVALID_MEASUREMENT
    }

    @UiThread
    private fun formatBmi(bmi: Double) = if (bmi >= 0) {
        bmiFormatter.format(bmi)
    } else {
        context.getString(R.string.no_value)
    }

    private val _bmiDescription = MutableLiveData<String>()
    val bmiDescription: LiveData<String> = _bmiDescription

    @UiThread
    private fun formatBmiDescription(bmi: Double) =
        context.getString(when {
            bmi < 0 -> R.string.no_value
            bmi < 18.5 -> R.string.bmi_underweight_description
            bmi in 18.5..25.0 -> R.string.bmi_normal_weight_description
            bmi in 25.0..30.0 -> R.string.bmi_overweight_description
            else -> R.string.bmi_obese_description
        })

    private val _formattedBfp = MutableLiveData<String>()
    val formattedBfp: LiveData<String> = _formattedBfp

    @UiThread
    private fun computeBfp(bmi: Double) = if (bmi >= 0 && birthDate != null && gender != null) {
        val today = LocalDate.now()
        val age = Period.between(birthDate, today)
        val ageInYears = age.years + (age.months / 12.0) + (age.days / today.lengthOfYear().toDouble())
        val genderValue = if (gender == Gender.MALE) 1 else 0

        ((1.39 * bmi) + (0.16 * ageInYears) - (10.34 * genderValue) - 9) / 100.0
    } else {
        INVALID_MEASUREMENT
    }

    @UiThread
    private fun formatBfp(bfp: Double) =  if (bfp >= 0) {
        bfpFormatter.format(bfp)
    } else {
        context.getString(R.string.no_value)
    }

    override fun onCreate(owner: LifecycleOwner) {
        Log.v(TAG, "> onCreate(...)")

        initFormatters()

        Log.v(TAG, "< onCreate(...)")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.v(TAG, "> onStart(...)")

        initFormatters()

        updateFormattedValues()

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

    @UiThread
    private fun initFormatters() {
        val locale = Locale.getDefault()

        val weightNumberFormat = NumberFormat.getInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        weightFormatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT, weightNumberFormat)

        val heightNumberFormat = NumberFormat.getInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        heightFormatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT, heightNumberFormat)

        bmiFormatter = NumberFormat.getInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }

        bfpFormatter = NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
    }

    @UiThread
    private fun updateFormattedValues() {
        val bmi = computeBmi()

        _formattedWeight.value = formatWeight()
        _formattedHeight.value = formatHeight()
        _formattedBmi.value = formatBmi(bmi)
        _bmiDescription.value = formatBmiDescription(bmi)
        _formattedBfp.value = formatBfp(computeBfp(bmi))
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

            _formattedBfp.value = formatBfp(computeBfp(computeBmi()))

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
            weight = snap?.getDouble(FirestoreRepository.WEIGHT_FIELD_VALUE) ?: INVALID_MEASUREMENT
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
            height = snap?.getDouble(FirestoreRepository.HEIGHT_FIELD_VALUE) ?: INVALID_MEASUREMENT
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
        if (weight.approximatelyEquals(this.weight)) {
            Log.d(TAG, "user weight didn't change; skip update")
            return
        }

        _updateWeightStatus.value = Resource.Loading<Any>()

        repo.setWeight(weight).addOnSuccessListener {
            Log.d(TAG, "user weight update success")
            this.weight = weight
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
        if (height.approximatelyEquals(this.height)) {
            Log.d(TAG, "user height didn't change; skip update")
            return
        }

        _updateHeightStatus.value = Resource.Loading<Any>()

        repo.setHeight(height).addOnSuccessListener {
            Log.d(TAG, "user height update success")
            this.height = height
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