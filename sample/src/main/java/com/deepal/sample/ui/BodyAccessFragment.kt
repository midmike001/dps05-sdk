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
import com.deepal.sample.databinding.FragmentBodyAccessBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

class BodyAccessFragment : Fragment() {

    private var _binding: FragmentBodyAccessBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBodyAccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Windows
        binding.btnWindowsOpenAll.setOnClickListener { viewModel.operateWindows(1) }
        binding.btnWindowsCloseAll.setOnClickListener { viewModel.operateWindows(2) }
        binding.btnWindowsStop.setOnClickListener { viewModel.operateWindows(0) }

        // Sunroof Shade
        binding.btnSunroofOpen.setOnClickListener { viewModel.operateSunroofShade(1) }
        binding.btnSunroofClose.setOnClickListener { viewModel.operateSunroofShade(2) }
        binding.btnSunroofStop.setOnClickListener { viewModel.operateSunroofShade(0) }

        // Tailgate & Locks
        binding.btnToggleTailgate.setOnClickListener { viewModel.toggleTailgate() }
        binding.btnToggleLocks.setOnClickListener { viewModel.toggleDoorLocks() }

        // Ambience & Purifier
        binding.btnAmbientCyan.setOnClickListener { viewModel.setAmbientPreset(1, 80) }
        binding.btnAmbientAmber.setOnClickListener { viewModel.setAmbientPreset(2, 60) }
        binding.btnAmbientEmerald.setOnClickListener { viewModel.setAmbientPreset(3, 70) }
        binding.btnAirPurifier.setOnClickListener { viewModel.toggleAirPurifier() }

        // Observe Telemetry
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.telemetry.collect { t ->
                    binding.tvWindowsStatus.text = "Windows: " +
                            "FL ${if (t.windowFlOpen) "Open" else "Closed"} | " +
                            "FR ${if (t.windowFrOpen) "Open" else "Closed"} | " +
                            "RL ${if (t.windowRlOpen) "Open" else "Closed"} | " +
                            "RR ${if (t.windowRrOpen) "Open" else "Closed"}"

                    binding.tvSunroofStatus.text = "Sunroof Blind: " + if (t.isSunroofOpen) "OPEN" else "CLOSED"

                    binding.btnToggleTailgate.text = "Tailgate: " + if (t.isTailgateOpen) "OPEN" else "CLOSED"
                    binding.btnToggleTailgate.setTextColor(if (t.isTailgateOpen) Color.parseColor("#FFB300") else Color.parseColor("#00E5FF"))

                    binding.btnToggleLocks.text = "Doors: " + if (t.isDoorLocked) "LOCKED" else "UNLOCKED"
                    binding.btnToggleLocks.setTextColor(if (t.isDoorLocked) Color.parseColor("#00E676") else Color.parseColor("#FF5252"))

                    binding.btnAirPurifier.text = "Purifier: " + if (t.isAirPurifierOn) "ON" else "OFF"
                    binding.btnAirPurifier.setTextColor(if (t.isAirPurifierOn) Color.parseColor("#00E676") else Color.parseColor("#94A3B8"))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
