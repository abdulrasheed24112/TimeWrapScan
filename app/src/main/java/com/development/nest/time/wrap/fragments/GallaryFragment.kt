package com.development.nest.time.wrap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.adapters.PagerAdapter
import com.development.nest.time.wrap.adapters.ViewpagerAdapter
import com.development.nest.time.wrap.databinding.FragmentDashBoardBinding
import com.development.nest.time.wrap.databinding.FragmentGallaryBinding
import com.google.android.material.tabs.TabLayoutMediator

class GallaryFragment : Fragment() {

    lateinit var binding:FragmentGallaryBinding
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewpagerAdapter(listOf<Fragment>(FilesFragment(), FilesFragment()), requireActivity())
        binding.viewPager.adapter = viewPagerAdapter
        initilizeTabLayout()
        binding.cvBack.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGallaryBinding.inflate(layoutInflater, container, false)
        return binding.root

    }
    private fun initilizeTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.images)
                }
                1 -> {
                    tab.text = getString(R.string.videos)
                }
            }
        }.attach()
    }


    companion object {
    }
}