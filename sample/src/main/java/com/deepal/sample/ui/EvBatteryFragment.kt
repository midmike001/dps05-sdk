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
import com.deepal.sample.databinding.FragmentEvBatteryBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

class EvBatteryFragment : Fragment() {

    private var _binding: FragmentEvBatteryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEvBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTogglePrecond.setOnClickListener {
            viewModel.toggleBatteryPreconditioning()
        }

        binding.btnCheckFeasibility.setOnClickListener {
            val distStr = binding.etTargetDistance.text?.toString() ?: "0"
            val dist = distStr.toFloatOrNull() ?: 0f
            val currentRange = viewModel.telemetry.value.remainingRangeKm
            val safetyBufferKm = 30f
            val needed = dist + safetyBufferKm

            if (currentRange >= needed) {
                val surplus = (currentRange - dist).toInt()
                binding.tvFeasibilityResult.text = "Status: Feasible! +${surplus}km reserve upon arrival."
                binding.tvFeasibilityResult.setTextColor(Color.parseColor("#00E676"))
            } else {
                val deficit = (needed - currentRange).toInt()
                binding.tvFeasibilityResult.text = "Warning: Insufficient range! Short by ${deficit}km. Waypoint fast charging recommended."
                binding.tvFeasibilityResult.setTextColor(Color.parseColor("#FF5252"))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.telemetry.collect { t ->
                    binding.tvEvSoc.text = "${t.batterySocPercent} %"
                    binding.tvEvRange.text = "${t.remainingRangeKm} km est."
                    binding.progressEv.progress = t.batterySocPercent

                    binding.btnTogglePrecond.text = "Battery Thermal Preconditioning: " +
                            if (t.isBatteryPreconditioning) "ACTIVE (Heating)" else "OFF (0x314006c6)"
                    binding.btnTogglePrecond.setTextColor(
                        if (t.isBatteryPreconditioning) Color.parseColor("#00E676") else Color.parseColor("#94A3B8")
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
