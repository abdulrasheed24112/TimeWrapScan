package com.development.nest.time.wrap.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.FragmentDashBoardBinding
import com.development.nest.time.wrap.utils.cameraPermission
import com.development.nest.time.wrap.utils.exitDialog
import com.development.nest.time.wrap.utils.requestPermission

class DashBoardFragment : Fragment() {

    lateinit var binding: FragmentDashBoardBinding

    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashBoardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvCamera.setOnClickListener {
            requireActivity().requestPermission(cameraPermission, requireActivity()) {
                goToNext()
            }
        }
        binding.cvGallary.setOnClickListener {
            navController.navigate(R.id.gallery_fragment)
        }
        binding.cvSettings.setOnClickListener {
            navController.navigate(R.id.settings_fragment)
        }
        binding.cvNavigation.setOnClickListener {
            navController.navigate(R.id.premium_fragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d("dashboardFragment", "handleOnBackPressed: back pressed")
                    requireActivity().exitDialog {
                        requireActivity().finishAffinity()
                    }
                }
            })
    }

    private fun goToNext() {
        navController.navigate(R.id.camera_fragment)
    }
}