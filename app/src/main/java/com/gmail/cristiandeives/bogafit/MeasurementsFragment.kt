package com.gmail.cristiandeives.bogafit

import android.icu.text.DecimalFormatSymbols
import android.icu.text.MeasureFormat
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

        val weightDouble = viewModel.weight.takeIf { it >= 0 } ?: DEFAULT_WEIGHT
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

        val heightDouble = viewModel.height.takeIf { it >= 0 } ?: DEFAULT_HEIGHT
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