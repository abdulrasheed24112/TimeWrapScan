package com.development.nest.time.wrap.fragments

import android.os.Bundle
import android.util.JsonReader
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.adapters.LanguagesAdapter
import com.development.nest.time.wrap.databinding.FragmentLanguageBinding
import com.development.nest.time.wrap.utils.AppEvents
import com.development.nest.time.wrap.utils.JsonArrayReader
import com.development.nest.time.wrap.utils.showToast

class LanguageFragment : Fragment() {

    lateinit var binding: FragmentLanguageBinding

    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLanguageBinding.inflate(layoutInflater, container, false)
        val jsonArray =JsonArrayReader.readJsonFromResources (resources, R.raw.locale)


        val languagesAdapter = LanguagesAdapter(jsonArray)
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = languagesAdapter
        }
        binding.cvBack.setOnClickListener {
            navController.popBackStack()
        }

        languagesAdapter.listener = object : AppEvents() {
            override fun onClick() {
//                requireActivity().showToast(getString(R.string.language_changed))
                ActivityCompat.recreate(requireActivity())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })
        return binding.root

    }



}