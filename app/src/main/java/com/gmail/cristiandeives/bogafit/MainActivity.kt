package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.gmail.cristiandeives.bogafit.databinding.ActivityMainBinding

@MainThread
class MainActivity : AppCompatActivity() {
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(...)")
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        setSupportActionBar(binding.actionBar)
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