package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.icu.text.DecimalFormatSymbols
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gmail.cristiandeives.bogafit.databinding.FragmentMeasurementsBinding
import java.util.Locale
import kotlin.math.roundToInt

@MainThread
class MeasurementsFragment : Fragment(),
    FragmentResultListener,
    MeasurementsActionHandler {

    private lateinit var binding: FragmentMeasurementsBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<MeasurementsViewModel>()

    private val weightFormatter = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT, NumberFormat.getInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    })
    private val heightFormatter = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT, NumberFormat.getInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    })
    private val bmiFormatter = NumberFormat.getInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    private val bfpFormatter = NumberFormat.getPercentInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    private val loadUserDataProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.loading_message))
            setCancelable(false)
        }
    }

    private val updateInfoProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.profile_edit_loading))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")
        binding = FragmentMeasurementsBinding.inflate(inflater, container, false)

        val view = binding.root
        Log.v(TAG, "< onCreateView(...): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner

            vm = viewModel
            action = this@MeasurementsFragment
        }

        lifecycle.addObserver(viewModel)

        viewModel.apply {
            loadUserDataStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> loadUserDataStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onLoadUserDataLoading()
                    is Resource.Success -> onLoadUserDataSuccess()
                    is Resource.Error -> onLoadUserDataError()
                    is Resource.Canceled -> onLoadUserDataCanceled()
                }

                if (res?.isFinished == true) {
                    loadUserDataProgressDialog.dismiss()
                }

                Log.v(TAG, "< loadUserDataStatus#onChanged(t=$res)")
            }

            weight.observe(viewLifecycleOwner) { weight ->
                Log.v(TAG, "> weight#onChanged(t=$weight)")

                val formattedWeight = if (weight > 0) {
                    weightFormatter.format(Measure(weight, MeasureUnit.KILOGRAM))
                } else {
                    getString(R.string.empty_string_value)
                }

                binding.weightText.text = formattedWeight

                Log.v(TAG, "< weight#onChanged(t=$weight)")
            }

            updateWeightStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> updateWeightStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onUpdateWeightLoading()
                    is Resource.Success -> onUpdateWeightSuccess()
                    is Resource.Error -> onUpdateWeightError()
                    is Resource.Canceled -> onUpdateWeightCanceled()
                }

                if (res?.isFinished == true) {
                    updateInfoProgressDialog.dismiss()
                }

                Log.v(TAG, "< updateWeightStatus#onChanged(t=$res)")
            }

            height.observe(viewLifecycleOwner) { height ->
                Log.v(TAG, "> height#onChanged(t=$height})")

                val formattedHeight = if (height >= 0) {
                    heightFormatter.format(Measure(height, MeasureUnit.METER))
                } else {
                    getString(R.string.empty_string_value)
                }

                binding.heightText.text = formattedHeight

                Log.v(TAG, "< height#onChanged(t=$height})")
            }

            updateHeightStatus.observe(viewLifecycleOwner) { res: Resource<*>? ->
                Log.v(TAG, "> updateHeightStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onUpdateHeightLoading()
                    is Resource.Success -> onUpdateHeightSuccess()
                    is Resource.Error -> onUpdateHeightError()
                    is Resource.Canceled -> onUpdateHeightCanceled()
                }

                if (res?.isFinished == true) {
                    updateInfoProgressDialog.dismiss()
                }

                Log.v(TAG, "< updateHeightStatus#onChanged(t=$res)")
            }

            bmi.observe(viewLifecycleOwner) { bmi ->
                Log.v(TAG, "> bmi#onChanged(t=$bmi)")

                val formattedBmi = if (bmi >= 0) {
                    bmiFormatter.format(bmi)
                } else {
                    getString(R.string.no_value)
                }

                val bmiDescriptionRes = when {
                    bmi < 0 -> R.string.no_value
                    bmi < 18.5 -> R.string.bmi_underweight_description
                    bmi in 18.5..25.0 -> R.string.bmi_normal_weight_description
                    bmi in 25.0..30.0 -> R.string.bmi_overweight_description
                    else -> R.string.bmi_obese_description
                }

                binding.apply {
                    bmiValue.text = formattedBmi
                    bmiDescriptionText.setText(bmiDescriptionRes)
                }

                Log.v(TAG, "< bmi#onChanged(t=$bmi)")
            }

            bfp.observe(viewLifecycleOwner) { bfp ->
                Log.v(TAG, "> bfp#onChanged(t=$bfp)")

                val formattedBfp = if (bfp >= 0) {
                    bfpFormatter.format(bfp)
                } else {
                    getString(R.string.no_value)
                }

                binding.bfpValue.text = formattedBfp

                Log.v(TAG, "< bfp#onChanged(t=$bfp)")
            }
        }

        parentFragmentManager.setFragmentResultListener(REQUEST_KEY_EDIT_WEIGHT, viewLifecycleOwner, this)
        parentFragmentManager.setFragmentResultListener(REQUEST_KEY_EDIT_HEIGHT, viewLifecycleOwner, this)

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        Log.v(TAG, "> onFragmentResult(requestKey=$requestKey, result=$result)")

        when (requestKey) {
            REQUEST_KEY_EDIT_WEIGHT -> {
                val weightInteger = result.getInt(BUNDLE_KEY_MEASUREMENT_VALUE0)
                val weightFraction = result.getInt(BUNDLE_KEY_MEASUREMENT_VALUE1)
                val w = weightInteger + (weightFraction / 10.0)

                Log.i(TAG, "user selected weight=$w kg")
                viewModel.updateWeight(w)
            }
            REQUEST_KEY_EDIT_HEIGHT -> {
                val heightMeter = result.getInt(BUNDLE_KEY_MEASUREMENT_VALUE0)
                val heightCentimeter = result.getInt(BUNDLE_KEY_MEASUREMENT_VALUE1)
                val h = heightMeter + (heightCentimeter / 100.0)

                Log.i(TAG, "user selected height=$h m")
                viewModel.updateHeight(h)
            }
            else -> throw IllegalArgumentException("unexpected request key [$requestKey]")
        }

        Log.v(TAG, "< onFragmentResult(requestKey=$requestKey, result=$result)")
    }

    @UiThread
    private fun onLoadUserDataLoading() {
        loadUserDataProgressDialog.show()
    }

    @UiThread
    private fun onLoadUserDataSuccess() {
        // do nothing
    }

    @UiThread
    private fun onLoadUserDataError() {
        requireView().showMessage(R.string.profile_load_user_data_error)
    }

    @UiThread
    private fun onLoadUserDataCanceled() {
        // do nothing
    }

    @UiThread
    private fun onUpdateWeightLoading() {
        updateInfoProgressDialog.show()
    }

    @UiThread
    private fun onUpdateWeightSuccess() {
        // do nothing
    }

    @UiThread
    private fun onUpdateWeightError() {
        requireView().showMessage(R.string.edit_weight_error)
    }

    @UiThread
    private fun onUpdateWeightCanceled() {
        // do nothing
    }

    @UiThread
    private fun onUpdateHeightLoading() {
        updateInfoProgressDialog.show()
    }

    @UiThread
    private fun onUpdateHeightSuccess() {
        // do nothing
    }

    @UiThread
    private fun onUpdateHeightError() {
        requireView().showMessage(R.string.edit_height_error)
    }

    @UiThread
    private fun onUpdateHeightCanceled() {
        // do nothing
    }

    private val decimalFormatSymbols = DecimalFormatSymbols.getInstance()
    private val measureFormat = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.NARROW)

    @UiThread
    override fun onWeightTextClick(view: View) {
        Log.d(TAG, "user tapped the weight text")

        val kilogramUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            measureFormat.getUnitDisplayName(MeasureUnit.KILOGRAM)
        } else {
            "kg"
        }

        val weightDouble = viewModel.weight.value?.takeIf { it >= 0 } ?: DEFAULT_WEIGHT
        val weight0 = weightDouble.toInt()
        val weight1 = ((weightDouble - weight0) * 10).roundToInt()

        val action = MeasurementsFragmentDirections.toEditWeight(
            REQUEST_KEY_EDIT_WEIGHT,
            R.string.weight_label,
            weight0, weight1,
            WEIGHT_MIN, WEIGHT_MAX,
            0, 9,
            decimalFormatSymbols.decimalSeparator.toString(), kilogramUnit,
        )
        navController.navigate(action)
    }

    @UiThread
    override fun onHeightTextClick(view: View) {
        Log.d(TAG, "user tapped the height text")

        val meterUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            measureFormat.getUnitDisplayName(MeasureUnit.METER)
        } else {
            "m"
        }

        val heightDouble = viewModel.height.value?.takeIf { it >= 0 } ?: DEFAULT_HEIGHT
        val height0 = heightDouble.toInt()
        val height1 = ((heightDouble - height0) * 100).roundToInt()

        val action = MeasurementsFragmentDirections.toEditWeight(
            REQUEST_KEY_EDIT_HEIGHT,
            R.string.height_label,
            height0, height1,
            HEIGHT_MIN, HEIGHT_MAX,
            0, 99,
            decimalFormatSymbols.decimalSeparator.toString(), meterUnit,
        )
        navController.navigate(action)
    }

    companion object {
        private val TAG = MeasurementsFragment::class.java.simpleName

        const val REQUEST_KEY_EDIT_WEIGHT = "edit_weight"
        const val REQUEST_KEY_EDIT_HEIGHT = "edit_height"
        const val BUNDLE_KEY_MEASUREMENT_VALUE0 = "value0"
        const val BUNDLE_KEY_MEASUREMENT_VALUE1 = "value1"

        private const val WEIGHT_MIN = 0
        private const val WEIGHT_MAX = 499
        private const val DEFAULT_WEIGHT = 70.0

        private const val HEIGHT_MIN = 0
        private const val HEIGHT_MAX = 2
        private const val DEFAULT_HEIGHT = 1.70
    }
}