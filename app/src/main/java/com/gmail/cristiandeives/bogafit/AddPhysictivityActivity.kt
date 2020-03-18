package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController

@MainThread
class AddPhysictivityActivity : AppCompatActivity(R.layout.activity_main) {
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(...)")
        super.onCreate(savedInstanceState)

        setupActionBarWithNavController(navController)

        Log.v(TAG, "< onCreate(...)")
    }

    companion object {
        private val TAG = AddPhysictivityActivity::class.java.simpleName
    }
}