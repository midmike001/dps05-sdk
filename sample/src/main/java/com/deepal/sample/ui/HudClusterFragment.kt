package com.deepal.sample.ui

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.deepal.sample.databinding.FragmentHudClusterBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel

class HudClusterFragment : Fragment() {

    private var _binding: FragmentHudClusterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    private var secondaryPresentation: HudSecondaryPresentation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHudClusterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // InCall Focus
        binding.btnRequestHudFocus.setOnClickListener { viewModel.requestHudFocus() }
        binding.btnAbandonHudFocus.setOnClickListener { viewModel.abandonHudFocus() }

        // Navigation Status
        binding.btnSendActiveStatus.setOnClickListener {
            viewModel.sendHudStatus(1) // 1 = Active
            viewModel.sendHudRemainInfo(distMeters = 4200, timeSec = 540)
        }
        binding.btnSendArrivedStatus.setOnClickListener {
            viewModel.sendHudStatus(2) // 2 = Arrived
        }
        binding.btnClearHud.setOnClickListener {
            viewModel.clearHud()
            secondaryPresentation?.updateManeuver("▲", "--", "Standby")
        }

        // Maneuver Icons (Transact 0x18)
        binding.btnTurnStraight.setOnClickListener {
            viewModel.sendHudManeuver(iconId = 1, distMeters = 500)
            secondaryPresentation?.updateManeuver("▲", "500m", "Straight Ahead")
        }
        binding.btnTurnLeft.setOnClickListener {
            viewModel.sendHudManeuver(iconId = 3, distMeters = 250)
            secondaryPresentation?.updateManeuver("↰", "250m", "Russian Blvd")
        }
        binding.btnTurnRight.setOnClickListener {
            viewModel.sendHudManeuver(iconId = 2, distMeters = 150)
            secondaryPresentation?.updateManeuver("↱", "150m", "Preah Monivong Blvd")
        }
        binding.btnTurnUturn.setOnClickListener {
            viewModel.sendHudManeuver(iconId = 6, distMeters = 80)
            secondaryPresentation?.updateManeuver("↶", "80m", "U-Turn Bay")
        }

        // Road Info (Transact 0x1a)
        binding.btnSendRoad1.setOnClickListener {
            viewModel.sendHudRoadInfo(nextRoad = "Preah Monivong Blvd", curRoad = "Russian Blvd")
        }
        binding.btnSendRoad2.setOnClickListener {
            viewModel.sendHudRoadInfo(nextRoad = "Russian Blvd", curRoad = "Monivong Blvd")
        }

        // Secondary Windshield Display Detection
        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)

        if (displays.isNotEmpty()) {
            val d = displays[0]
            binding.tvSecondaryDisplayInfo.text = "Detected Physical Display: ${d.name} (${d.width}x${d.height})"
        } else {
            binding.tvSecondaryDisplayInfo.text = "No hardware secondary display attached. (Can be tested via Android Developer Settings -> Simulate Secondary Displays -> 800x480)"
        }

        binding.btnLaunchPresentation.setOnClickListener {
            val targetDisplay: Display? = if (displays.isNotEmpty()) displays[0] else displayManager.displays.firstOrNull()
            if (targetDisplay != null) {
                try {
                    secondaryPresentation?.dismiss()
                    secondaryPresentation = HudSecondaryPresentation(requireActivity(), targetDisplay).apply {
                        show()
                    }
                    Toast.makeText(requireContext(), "Launched 800x480 Windshield Presentation", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Launch presentation failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "No display available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        secondaryPresentation?.dismiss()
        secondaryPresentation = null
        _binding = null
    }
}
