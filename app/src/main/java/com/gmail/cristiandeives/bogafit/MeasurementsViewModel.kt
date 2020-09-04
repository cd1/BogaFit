package com.gmail.cristiandeives.bogafit

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmail.cristiandeives.bogafit.data.FirestoreRepository
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

@MainThread
class MeasurementsViewModel : ViewModel(),
    DefaultLifecycleObserver {

    private val repo = FirestoreRepository.getInstance()

    private lateinit var weightFormatter: MeasureFormat
    private lateinit var heightFormatter: MeasureFormat
    private lateinit var bmiFormatter: NumberFormat

    init {
        initFormatters()
    }

    var weight = DEFAULT_WEIGHT
        private set(value) {
            field = value

            _formattedWeight.value = formatWeight(value)
            _bmi.value = formatBmi(weight = value)
        }
    private val _formattedWeight = MutableLiveData<String>()
    val formattedWeight: LiveData<String> = _formattedWeight

    var height = DEFAULT_HEIGHT
        private set(value) {
            field = value

            _formattedHeight.value = formatHeight(value)
            _bmi.value = formatBmi(height = value)
        }
    private val _formattedHeight = MutableLiveData<String>()
    val formattedHeight: LiveData<String> = _formattedHeight

    private val _bmi = MutableLiveData<String>()
    val bmi: LiveData<String> = _bmi

    override fun onStart(owner: LifecycleOwner) {
        Log.v(TAG, "> onStart(...)")

        initFormatters()

        _formattedWeight.value = formatWeight()
        _formattedHeight.value = formatHeight()
        _bmi.value = formatBmi()

        startListeningToWeight()
        startListeningToHeight()

        Log.v(TAG, "< onStart(...)")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.v(TAG, "> onStop(...)")

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
            weight = snap?.getDouble(FirestoreRepository.WEIGHT_FIELD_VALUE) ?: DEFAULT_WEIGHT
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
            height = snap?.getDouble(FirestoreRepository.HEIGHT_FIELD_VALUE) ?: DEFAULT_HEIGHT
        }
    }

    @UiThread
    private fun stopListeningToHeight() {
        heightListener?.remove()
    }

    @UiThread
    private fun formatWeight(weight: Double = this.weight) =
        weightFormatter.format(Measure(weight, MeasureUnit.KILOGRAM))

    @UiThread
    private fun formatHeight(height: Double = this.height) =
        heightFormatter.format(Measure(height, MeasureUnit.METER))

    @UiThread
    private fun formatBmi(weight: Double = this.weight, height: Double = this.height) =
        bmiFormatter.format(weight / (height * height))

    private val _updateWeightStatus = MutableLiveData<Resource<*>>()
    val updateWeightStatus: LiveData<Resource<*>> = _updateWeightStatus

    @UiThread
    fun updateWeight(weight: Double) {
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

        private const val DEFAULT_WEIGHT = 70.0
        private const val DEFAULT_HEIGHT = 1.70
    }
}