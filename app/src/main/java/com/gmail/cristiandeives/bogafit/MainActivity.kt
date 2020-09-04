package com.gmail.cristiandeives.bogafit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.gmail.cristiandeives.bogafit.databinding.ActivityMainBinding

@MainThread
class MainActivity : AppCompatActivity() {
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(...)")
        super.onCreate(savedInstanceState)

        if (!viewModel.isUserAuthenticated()) {
            Log.d(TAG, "user is not authenticated; launching authentication screen")

            val intent = Intent(this, AuthenticationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            startActivity(intent)

            finish()
            return
        }

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        setSupportActionBar(binding.actionBar)
        setupActionBarWithNavController(navController, AppBarConfiguration(TOP_LEVEL_FRAGMENTS))

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

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

        // set of fragments which shouldn't have the up arrow in the Action Bar
        private val TOP_LEVEL_FRAGMENTS = setOf(
            R.id.list_physictivity_fragment,
            R.id.measurements_fragment,
            R.id.profile_fragment
        )
    }
}