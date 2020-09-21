package com.gmail.cristiandeives.bogafit

import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference

@MainThread
class SettingsFragment : PreferenceFragmentCompat(),
    Preference.SummaryProvider<EditTextPreference>,
    Preference.OnPreferenceChangeListener {

    private val viewModel by viewModels<SettingsViewModel>()

    private lateinit var physictivityGoalFormatter: NumberFormat
    private lateinit var enableGoalPref: SwitchPreference
    private lateinit var goalValuePref: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Log.v(TAG, "> onCreatePreferences(savedInstanceState=$savedInstanceState, rootKey=$rootKey)")

        setPreferencesFromResource(R.xml.preference_settings, rootKey)

        Log.v(TAG, "< onCreatePreferences(savedInstanceState=$savedInstanceState, rootKey=$rootKey)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        enableGoalPref = findPreference(SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_ENABLED)
            ?: throw IllegalStateException("could not find preference ${SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_ENABLED}")
        goalValuePref = findPreference(SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_VALUE)
            ?: throw IllegalStateException("could not find preference ${SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_VALUE}")

        enableGoalPref.onPreferenceChangeListener = this

        goalValuePref.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            summaryProvider = this@SettingsFragment
            onPreferenceChangeListener = this@SettingsFragment
        }

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onStart() {
        Log.v(TAG, "> onStart()")
        super.onStart()

        initFormatter()
        goalValuePref.isEnabled = viewModel.isPhysictivityGoalEnabled

        Log.v(TAG, "< onStart()")
    }

    override fun provideSummary(preference: EditTextPreference): String =
        physictivityGoalFormatter.format(preference.text.toInt())

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        Log.v(TAG, "> onPreferenceChange(preference=$preference, newValue=$newValue)")

        val shouldChange = when (preference.key) {
            SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_ENABLED -> {
                Log.i(TAG, "user changed physictivity goal status to '$newValue'" )
                val isGoalEnabled = (newValue as Boolean)

                goalValuePref.isEnabled = isGoalEnabled

                true
            }
            SettingsViewModel.PREF_KEY_PHYSICTIVITY_GOAL_VALUE -> {
                Log.i(TAG, "user changed physictivity goal value to '$newValue'")
                val goal = (newValue as String).toIntOrNull()

                return (goal != null) && SettingsViewModel.isPhysictivityGoalValid(goal)
            }
            else -> throw IllegalArgumentException("unexpected changed preference [$preference]")
        }

        Log.v(TAG, "< onPreferenceChange(preference=$preference, newValue=$newValue): $shouldChange")
        return shouldChange
    }

    @UiThread
    private fun initFormatter() {
        physictivityGoalFormatter = NumberFormat.getIntegerInstance()
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }
}