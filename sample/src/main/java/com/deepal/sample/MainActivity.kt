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

        // Navigation Tabs Setup (6 Domains)
        val tabTitles = listOf(
            "Telemetry",
            "Climate & Comfort",
            "Body & Windows",
            "EV & Battery",
            "AR-HUD & InCall",
            "Scenes & Voice"
        )

        for (title in tabTitles) {
            binding.tabNav.addTab(binding.tabNav.newTab().setText(title))
        }

        binding.tabNav.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val fragment: Fragment = when (tab?.position) {
                    0 -> TelemetryFragment()
                    1 -> ClimateSeatsFragment()
                    2 -> BodyAccessFragment()
                    3 -> EvBatteryFragment()
                    4 -> HudClusterFragment()
                    5 -> ScenesVoiceFragment()
                    else -> TelemetryFragment()
                }
                replaceFragment(fragment)
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

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
