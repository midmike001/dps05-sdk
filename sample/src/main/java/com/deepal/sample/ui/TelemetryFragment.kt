package com.deepal.sample.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.deepal.sample.databinding.FragmentTelemetryBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

class TelemetryFragment : Fragment() {

    private var _binding: FragmentTelemetryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelemetryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.telemetry.collect { telemetry ->
                    binding.tvSpeedValue.text = telemetry.speedKmh.toInt().toString()

                    // Gear selection styling
                    binding.tvGearP.setTextColor(if (telemetry.gear == "P") Color.parseColor("#00E5FF") else Color.parseColor("#334155"))
                    binding.tvGearR.setTextColor(if (telemetry.gear == "R") Color.parseColor("#00E5FF") else Color.parseColor("#334155"))
                    binding.tvGearN.setTextColor(if (telemetry.gear == "N") Color.parseColor("#00E5FF") else Color.parseColor("#334155"))
                    binding.tvGearD.setTextColor(if (telemetry.gear == "D") Color.parseColor("#00E5FF") else Color.parseColor("#334155"))

                    // Battery & Range
                    binding.tvBatterySoc.text = "${telemetry.batterySocPercent}%"
                    binding.progressBattery.progress = telemetry.batterySocPercent
                    binding.tvRange.text = "${telemetry.remainingRangeKm} km"
                    binding.tvOdometer.text = "Odo: ${"%.1f".format(telemetry.odometerKm)} km"

                    // Connection indicator
                    if (telemetry.isVirtualCarConnected) {
                        binding.viewConnectionDot.setBackgroundColor(Color.parseColor("#00E676"))
                        binding.tvConnectionStatus.text = "VirtualCar Service: CONNECTED"
                    } else {
                        binding.viewConnectionDot.setBackgroundColor(Color.parseColor("#FF5252"))
                        binding.tvConnectionStatus.text = "VirtualCar Service: NOT FOUND (Binder Idle)"
                    }

                    // Environment & Chassis
                    binding.tvExteriorTemp.text = "Exterior: ${telemetry.exteriorTempC}°C"
                    binding.tvDriveMode.text = "Mode: ${telemetry.driveMode}"
                    binding.tvRainSensor.text = "Rain Sensor: " + when (telemetry.rainSensorState) {
                        2 -> "Light Rain"
                        3 -> "Heavy Rain"
                        else -> "Dry"
                    }
                    binding.tvPreconditioning.text = "Battery Precond: " + if (telemetry.isBatteryPreconditioning) "ON" else "OFF"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
