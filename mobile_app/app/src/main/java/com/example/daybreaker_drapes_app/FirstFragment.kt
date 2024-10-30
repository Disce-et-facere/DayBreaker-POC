package com.example.daybreaker_drapes_app

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.daybreaker_drapes_app.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private lateinit var deviceViewModel: DeviceViewModel
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        deviceViewModel = ViewModelProvider(requireActivity()).get(DeviceViewModel::class.java)

        // Observe the devices LiveData
        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            // Clear existing views to avoid duplicates
            binding.cardContainer.removeAllViews()

            // Add a CardView for each device (message) in the list
            for (device in devices) {
                val cardView = createDeviceCard(device)
                binding.cardContainer.addView(cardView)
            }
        }
    }

    private fun createDeviceCard(deviceDisplayName: String): CardView {
        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                240
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 16f
            cardElevation = 4f
            setCardBackgroundColor(resources.getColor(R.color.Brown_orange, null))
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
        }

        val textView = TextView(requireContext()).apply {
            text = deviceDisplayName
            textSize = 20f
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.white, null))
        }

        layout.addView(textView)
        cardView.addView(layout)

        // When a card is clicked, retrieve the correct MAC by display name and navigate
        cardView.setOnClickListener {
            // Look up the MAC address by the displayed alias or MAC name
            val macAddress = deviceViewModel.macAddresses.find {
                it == deviceDisplayName || deviceViewModel.getAlias(deviceViewModel.macAddresses.indexOf(it)) == deviceDisplayName
            }

            macAddress?.let { mac ->
                val index = deviceViewModel.macAddresses.indexOf(mac) // Find index of the MAC address
                deviceViewModel.selectMacAddress(index) // Set the selected device by index
                findNavController().navigate(R.id.action_FirstFragment_to_DeviceDetailFragment)
            } ?: run {
                Log.e("FirstFragment", "Device not found: $deviceDisplayName")
            }
        }

        return cardView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
