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
import com.deepal.sample.databinding.FragmentClimateSeatsBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

class ClimateSeatsFragment : Fragment() {

    private var _binding: FragmentClimateSeatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClimateSeatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Climate Power & Temp Buttons
        binding.btnClimatePower.setOnClickListener {
            viewModel.toggleClimatePower()
        }

        binding.btnDriverTempMinus.setOnClickListener {
            val cur = viewModel.telemetry.value.climateTempC
            viewModel.setDriverTemp((cur - 0.5f).coerceAtLeast(17.5f))
        }

        binding.btnDriverTempPlus.setOnClickListener {
            val cur = viewModel.telemetry.value.climateTempC
            viewModel.setDriverTemp((cur + 0.5f).coerceAtMost(32.5f))
        }

        binding.btnPassTempMinus.setOnClickListener {
            val cur = viewModel.telemetry.value.passengerTempC
            viewModel.setPassengerTemp((cur - 0.5f).coerceAtLeast(17.5f))
        }

        binding.btnPassTempPlus.setOnClickListener {
            val cur = viewModel.telemetry.value.passengerTempC
            viewModel.setPassengerTemp((cur + 0.5f).coerceAtMost(32.5f))
        }

        // Fan Speed Slider
        binding.sliderFanSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setFanSpeed(value.toInt())
            }
        }

        // Toggles
        binding.btnToggleAc.setOnClickListener { viewModel.toggleAc() }
        binding.btnToggleAutoClimate.setOnClickListener { viewModel.toggleAutoClimate() }
        binding.btnFrontDefrost.setOnClickListener { viewModel.toggleFrontDefrost() }
        binding.btnRearDefrost.setOnClickListener { viewModel.toggleRearDefrost() }

        // Airflow Vent Direction (8=Defrost, 9=Face, 10=Feet, 11=Dual)
        binding.btnWindFace.setOnClickListener { viewModel.setWindDirection(9) }
        binding.btnWindFeet.setOnClickListener { viewModel.setWindDirection(10) }
        binding.btnWindDual.setOnClickListener { viewModel.setWindDirection(11) }
        binding.btnWindDefrost.setOnClickListener { viewModel.setWindDirection(8) }

        // Seat Comfort (Driver & Passenger)
        binding.btnDriverHeat.setOnClickListener {
            val next = (viewModel.telemetry.value.driverSeatHeat + 1) % 4
            viewModel.setDriverSeatHeat(next)
        }

        binding.btnDriverVent.setOnClickListener {
            val next = (viewModel.telemetry.value.driverSeatVent + 1) % 4
            viewModel.setDriverSeatVent(next)
        }

        binding.btnPassHeat.setOnClickListener {
            val next = (viewModel.telemetry.value.passengerSeatHeat + 1) % 4
            viewModel.setPassengerSeatHeat(next)
        }

        binding.btnPassVent.setOnClickListener {
            val next = (viewModel.telemetry.value.passengerSeatVent + 1) % 4
            viewModel.setPassengerSeatVent(next)
        }

        binding.btnMassage.setOnClickListener {
            viewModel.toggleMassage(mode = 2, level = 2)
        }

        binding.btnSteeringHeat.setOnClickListener {
            viewModel.toggleSteeringHeat()
        }

        // Memory Presets
        binding.btnPresetM1.setOnClickListener {
            viewModel.setDriverTemp(21.5f)
            viewModel.setFanSpeed(2)
            viewModel.setDriverSeatHeat(0)
            viewModel.setDriverSeatVent(2)
            viewModel.logAction("Applied Comfort Preset M1 (Cool & Crisp: 21.5°C)")
        }

        binding.btnPresetM2.setOnClickListener {
            viewModel.setDriverTemp(23.0f)
            viewModel.setFanSpeed(3)
            viewModel.setDriverSeatHeat(0)
            viewModel.setDriverSeatVent(1)
            viewModel.logAction("Applied Comfort Preset M2 (Balanced: 23.0°C)")
        }

        binding.btnPresetM3.setOnClickListener {
            viewModel.setDriverTemp(25.0f)
            viewModel.setFanSpeed(3)
            viewModel.setDriverSeatHeat(2)
            viewModel.setDriverSeatVent(0)
            viewModel.logAction("Applied Comfort Preset M3 (Warm Cozy: 25.0°C)")
        }

        // Collect Telemetry State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.telemetry.collect { t ->
                    binding.btnClimatePower.text = "PWR: " + if (t.isClimatePowerOn) "ON" else "OFF"
                    binding.btnClimatePower.setTextColor(if (t.isClimatePowerOn) Color.parseColor("#00E676") else Color.parseColor("#94A3B8"))

                    binding.tvDriverTemp.text = "${"%.1f".format(t.climateTempC)}°C"
                    binding.tvPassengerTemp.text = "${"%.1f".format(t.passengerTempC)}°C"

                    binding.tvFanSpeedLabel.text = "Blower Fan Speed: ${t.fanSpeed} / 8"
                    if (binding.sliderFanSpeed.value.toInt() != t.fanSpeed) {
                        binding.sliderFanSpeed.value = t.fanSpeed.toFloat()
                    }

                    binding.btnToggleAc.text = "A/C: " + if (t.isAcOn) "ON" else "OFF"
                    binding.btnToggleAc.setTextColor(if (t.isAcOn) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))

                    binding.btnToggleAutoClimate.text = "AUTO: " + if (t.isAutoClimateOn) "ON" else "OFF"
                    binding.btnToggleAutoClimate.setTextColor(if (t.isAutoClimateOn) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))

                    binding.btnFrontDefrost.text = "FRONT: " + if (t.isFrontDefrostOn) "ON" else "OFF"
                    binding.btnFrontDefrost.setTextColor(if (t.isFrontDefrostOn) Color.parseColor("#FFB300") else Color.parseColor("#94A3B8"))

                    binding.btnRearDefrost.text = "REAR: " + if (t.isRearDefrostOn) "ON" else "OFF"
                    binding.btnRearDefrost.setTextColor(if (t.isRearDefrostOn) Color.parseColor("#FFB300") else Color.parseColor("#94A3B8"))

                    // Wind Direction highlights
                    binding.btnWindFace.setTextColor(if (t.windDirection == 9) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))
                    binding.btnWindFeet.setTextColor(if (t.windDirection == 10) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))
                    binding.btnWindDual.setTextColor(if (t.windDirection == 11) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))
                    binding.btnWindDefrost.setTextColor(if (t.windDirection == 8) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))

                    // Seats
                    binding.btnDriverHeat.text = "Heat: ${t.driverSeatHeat}"
                    binding.btnDriverHeat.setTextColor(if (t.driverSeatHeat > 0) Color.parseColor("#FF5252") else Color.parseColor("#94A3B8"))

                    binding.btnDriverVent.text = "Vent: ${t.driverSeatVent}"
                    binding.btnDriverVent.setTextColor(if (t.driverSeatVent > 0) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))

                    binding.btnPassHeat.text = "Heat: ${t.passengerSeatHeat}"
                    binding.btnPassHeat.setTextColor(if (t.passengerSeatHeat > 0) Color.parseColor("#FF5252") else Color.parseColor("#94A3B8"))

                    binding.btnPassVent.text = "Vent: ${t.passengerSeatVent}"
                    binding.btnPassVent.setTextColor(if (t.passengerSeatVent > 0) Color.parseColor("#00E5FF") else Color.parseColor("#94A3B8"))

                    binding.btnMassage.text = "Massage: " + if (t.isSeatMassageOn) "ON" else "OFF"
                    binding.btnMassage.setTextColor(if (t.isSeatMassageOn) Color.parseColor("#00E676") else Color.parseColor("#94A3B8"))

                    binding.btnSteeringHeat.text = "Wheel Heat: " + if (t.isSteeringWheelHeatOn) "ON" else "OFF"
                    binding.btnSteeringHeat.setTextColor(if (t.isSteeringWheelHeatOn) Color.parseColor("#FF5252") else Color.parseColor("#94A3B8"))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
