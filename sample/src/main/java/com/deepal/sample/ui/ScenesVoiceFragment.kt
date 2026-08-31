package com.deepal.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.deepal.sample.databinding.FragmentScenesVoiceBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel

class ScenesVoiceFragment : Fragment() {

    private var _binding: FragmentScenesVoiceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleControlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScenesVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Smart Cockpit Scenes
        binding.btnSceneRapidCool.setOnClickListener {
            viewModel.triggerScene("RAPID_COOL")
        }

        binding.btnSceneNap.setOnClickListener {
            viewModel.triggerScene("NAP")
        }

        binding.btnSceneDefrost.setOnClickListener {
            viewModel.triggerScene("DEFROST")
        }

        binding.btnSceneCamp.setOnClickListener {
            viewModel.triggerScene("CAMP")
        }

        // Rain Guardian
        binding.btnExecuteRainGuardian.setOnClickListener {
            viewModel.triggerRainGuardian()
        }

        // Voice Assistant Command Emulations
        binding.btnVoiceSunroof.setOnClickListener {
            viewModel.logAction("Voice Wake: 'Hello Deepal, open the sunroof shade'")
            viewModel.operateSunroofShade(1)
        }

        binding.btnVoiceMassage.setOnClickListener {
            viewModel.logAction("Voice Wake: 'Hello Deepal, turn on driver seat massage'")
            viewModel.toggleMassage(mode = 2, level = 3)
        }

        binding.btnVoiceCool.setOnClickListener {
            viewModel.logAction("Voice Wake: 'Hello Deepal, I'm feeling hot'")
            viewModel.triggerScene("RAPID_COOL")
        }

        binding.btnVoiceLock.setOnClickListener {
            viewModel.logAction("Voice Wake: 'Hello Deepal, lock all doors'")
            viewModel.toggleDoorLocks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
