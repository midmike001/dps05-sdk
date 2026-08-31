package com.deepal.sample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.deepal.sample.databinding.ActivityMainBinding
import com.deepal.sample.ui.BodyAccessFragment
import com.deepal.sample.ui.ClimateSeatsFragment
import com.deepal.sample.ui.EvBatteryFragment
import com.deepal.sample.ui.HudClusterFragment
import com.deepal.sample.ui.ScenesVoiceFragment
import com.deepal.sample.ui.TelemetryFragment
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: VehicleControlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Simulation Mode Toggle
        binding.switchSimMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSimulatedMode(isChecked)
        }

        // Live Action Log Banner
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastActionLog.collect { log ->
                    binding.tvActionLog.text = log
                }
            }
        }

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_telemetry -> TelemetryFragment()
                R.id.nav_climate -> ClimateSeatsFragment()
                R.id.nav_body -> BodyAccessFragment()
                R.id.nav_ev -> EvBatteryFragment()
                R.id.nav_hud -> HudClusterFragment()
                R.id.nav_scenes -> ScenesVoiceFragment()
                else -> TelemetryFragment()
            }
            replaceFragment(fragment)
            true
        }

        // Default initial fragment
        if (savedInstanceState == null) {
            replaceFragment(TelemetryFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
