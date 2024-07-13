package com.development.nest.time.wrap.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.FragmentSettingsBinding
import com.development.nest.time.wrap.utils.rateUsDialog
import com.development.nest.time.wrap.utils.safeClick
import com.development.nest.time.wrap.utils.sendMail
import com.development.nest.time.wrap.utils.shareApp
import com.google.android.datatransport.BuildConfig


class SettingsFragment : Fragment() {

    lateinit var binding:FragmentSettingsBinding

    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentSettingsBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })

        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        binding.clShare.safeClick({
            shareApp(requireContext())
        })
        binding.cvBack.setOnClickListener {
            navController.navigateUp()
        }
        binding.clRateUs.setOnClickListener {
            requireActivity().rateUsDialog()
        }
        binding.clPrivacy.setOnClickListener {
            val url = "https://google.com"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.clFeed.setOnClickListener {
            requireActivity().sendMail(
                title = getString(R.string.app_name), bodyMessage = getString(R.string.feedback)
            )
        }
        binding.clLanguage.setOnClickListener {
            navController.navigate(R.id.languages_fragment)
        }
        /*binding.purchasePpremium.setOnClickListener {
            navController.navigate(R.id.premium_fragment)
        }*/

        return binding.root


    }


}