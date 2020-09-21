package com.gmail.cristiandeives.bogafit

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager

@MainThread
class SettingsViewModel(private val context: Application) : AndroidViewModel(context) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    val isPhysictivityGoalEnabled: Boolean
        get() = sharedPref.getBoolean(PREF_KEY_PHYSICTIVITY_GOAL_ENABLED, context.resources.getBoolean(R.bool.default_physictivity_goal_enabled))

    companion object {
        const val PREF_KEY_PHYSICTIVITY_GOAL_ENABLED = "physictivity_goal_enabled"
        const val PREF_KEY_PHYSICTIVITY_GOAL_VALUE = "physictivity_goal"

        fun isPhysictivityGoalValid(value: Int) = (value in 1..366)
    }
}