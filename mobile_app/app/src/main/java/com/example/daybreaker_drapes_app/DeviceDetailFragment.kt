package com.example.daybreaker_drapes_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.daybreaker_drapes_app.databinding.FragmentDeviceDetailBinding

class DeviceDetailFragment : Fragment() {

    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var deviceViewModel: DeviceViewModel
    private var deviceIndex: Int? = null // Store the index associated with this device

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceViewModel = ViewModelProvider(requireActivity())[DeviceViewModel::class.java]

        // Observe the selected MAC address to set the index for this device
        deviceViewModel.selectedMacAddress.observe(viewLifecycleOwner) { mac ->
            deviceIndex = deviceViewModel.macAddresses.indexOf(mac)
            deviceIndex?.let { index ->
                val aliasOrMac = deviceViewModel.getAlias(index)
                Log.d("MQTT", "Using MAC address: $mac at index: $index")
                binding.editTextMacAddress.setText(aliasOrMac) // Show alias if available, fallback to MAC
            }
        }

        binding.buttonGoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonOpenDrape.setOnClickListener {
            sendDrapeCommand("up")
            binding.textViewState.text = "State: Open"
        }

        binding.buttonCloseDrape.setOnClickListener {
            sendDrapeCommand("down")
            binding.textViewState.text = "State: Closed"
        }

        binding.buttonSaveSettings.setOnClickListener {
            val alias = binding.editTextMacAddress.text.toString()
            deviceIndex?.let { index ->
                val mac = deviceViewModel.getMac(index) // Retrieve MAC using index
                mac?.let {
                    if (alias.isNotEmpty() && deviceViewModel.isAliasUnique(alias, it)) {
                        // Publish alias to MQTT topic and update the alias in ViewModel
                        deviceViewModel.publishToTopic("msgHandler/$it/alias", alias)
                        deviceViewModel.addOrUpdateDevice(it, alias) // Update alias in ViewModel
                        binding.editTextMacAddress.setText(alias)
                        findNavController().navigateUp()
                    } else {
                        Log.d("MQTT", "Alias \"$alias\" already in use or invalid.")
                        binding.editTextMacAddress.error = "Alias is already in use or invalid"
                    }
                }
            }
        }
    }

    private fun sendDrapeCommand(command: String) {
        // Use the MAC address from the index for sending commands
        deviceIndex?.let { index ->
            val mac = deviceViewModel.getMac(index)
            mac?.let {
                Log.d("MQTT", "Sending command to MAC address: $it")
                deviceViewModel.publishToTopic("msgHandler/command/$it", command)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
