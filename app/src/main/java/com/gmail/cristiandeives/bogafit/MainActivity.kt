package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController

@MainThread
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(...)")
        super.onCreate(savedInstanceState)

        setupActionBarWithNavController(navController)

        Log.v(TAG, "< onCreate(...)")
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.v(TAG, "> onSupportNavigateUp()")

        val navigatedUp = navController.navigateUp() || super.onSupportNavigateUp()

        Log.v(TAG, "< onSupportNavigateUp(): $navigatedUp")
        return navigatedUp
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}