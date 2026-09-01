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
import com.deepal.sample.databinding.FragmentScenesVoiceBinding
import com.deepal.sample.viewmodel.VehicleControlViewModel
import kotlinx.coroutines.launch

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

        // Outside Speaker & TTS
        binding.btnOutsideMusic.setOnClickListener {
            viewModel.toggleOutsideMusic()
        }

        binding.btnOutsideTts.setOnClickListener {
            viewModel.playOutsideTts("Attention: Deepal S05 vehicle reversing, please watch out.")
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

        // Observe Telemetry for outside speaker state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.telemetry.collect { t ->
                    binding.btnOutsideMusic.text = "Outside Music: " + if (t.isOutsideMusicPlaying) "PLAYING" else "STOPPED"
                    binding.btnOutsideMusic.setTextColor(
                        if (t.isOutsideMusicPlaying) Color.parseColor("#00E676") else Color.parseColor("#94A3B8")
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
