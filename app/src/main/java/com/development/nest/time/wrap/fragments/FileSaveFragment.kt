package com.development.nest.time.wrap.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.FragmentFileSaveBinding
import com.development.nest.time.wrap.databinding.FragmentFilesBinding
import com.development.nest.time.wrap.fragments.CameraFragment.Companion.bitmap1
import com.development.nest.time.wrap.utils.Constants
import com.development.nest.time.wrap.utils.confirmDiscardDialog
import com.development.nest.time.wrap.utils.onBackPressedCustomAction
import com.development.nest.time.wrap.utils.safeClick
import com.development.nest.time.wrap.utils.shareFile
import com.development.nest.time.wrap.utils.shareOnSocialMedia
import com.development.nest.time.wrap.viewmodel.MainViewModel
import java.io.File


class SaveFileFragment : Fragment() {

    lateinit var binding: FragmentFileSaveBinding
    private val viewModel: MainViewModel by activityViewModels()
    private var isSaved = false
    val args: SaveFileFragmentArgs by navArgs()
    var path = ""

    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileSaveBinding.inflate(layoutInflater, container, false)

//        isVideo = arguments?.getBoolean("isVideo", false) ?: false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    var isVideo = false

    private fun initViews() {
        /*args.image?.let {
        }*/
        bitmap1?.let { bitmap ->
            Glide.with(binding.previewImage.context).load(bitmap).into(binding.previewImage)
        }
        args.video?.let {
            path = it
            isVideo = true
            binding.btnSave.text = getString(R.string.save_video)
            binding.videoView.visibility = View.VISIBLE
            binding.videoView.setVideoURI(Uri.parse(it))

            binding.videoView.start()
            binding.videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
            }
        }

        onBackPressedCustomAction {
            if (!isSaved) {
                requireActivity().confirmDiscardDialog {
                    if (isVideo) {
                        args.video?.let {
                            val file = File(it)
                            file.delete()
                        }
                    }
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
        }

        binding.cvBack.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }

        binding.btnSave.setOnClickListener {
            isSaved = true
            if (isVideo) {
                viewModel.updatedList.postValue(true)
            } else {
                bitmap1?.let { bitmap ->
                    path = viewModel.saveBitmap(bitmap, context)
                }
            }
            binding.shareLay.visibility = View.VISIBLE
            binding.btnSave.visibility = View.GONE
        }

        binding.fbShare.safeClick({
            context?.let { it1 ->
                shareOnSocialMedia(
                    it1, path, Constants.FACEBOOK_PACKAGE
                )
            }
        }, 1000)
        binding.waShare.safeClick({
            context?.let { it1 ->
                shareOnSocialMedia(
                    it1, path, Constants.WHATSAPP_PACKAGE
                )
            }
        }, 1000)

        binding.twitterShare.safeClick({
            context?.let { it1 ->
                shareOnSocialMedia(
                    it1, path, Constants.TWITTER_PACKAGE
                )
            }
        }, 1000)
        binding.allShare.safeClick({ requireActivity().shareFile(path) }, 1000)

        /*requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })*/
    }

    override fun onPause() {
        super.onPause()
        binding.videoView.suspend()
    }

    override fun onResume() {
        super.onResume()
        binding.videoView.resume()
    }

}