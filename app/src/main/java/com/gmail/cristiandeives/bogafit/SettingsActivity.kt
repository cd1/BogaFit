package com.gmail.cristiandeives.bogafit

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.gmail.cristiandeives.bogafit.databinding.ActivitySettingsBinding

@MainThread
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(...)")
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        setSupportActionBar(binding.actionBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Log.v(TAG, "< onCreate(...)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=${resources.getResourceName(item.itemId)})")

        val consumed = when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

        Log.v(TAG, "< onOptionsItemSelected(item=${resources.getResourceName(item.itemId)}): $consumed")
        return consumed
    }

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
    }
}