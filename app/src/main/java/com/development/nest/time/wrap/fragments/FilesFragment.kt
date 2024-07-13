package com.development.nest.time.wrap.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.adapters.GalleryAdapter
import com.development.nest.time.wrap.databinding.FragmentFilesBinding
import com.development.nest.time.wrap.utils.AppEvents
import com.development.nest.time.wrap.utils.cameraPermission
import com.development.nest.time.wrap.utils.confirmDeleteDialog
import com.development.nest.time.wrap.utils.copyFile
import com.development.nest.time.wrap.utils.itemMenuBottomSheetDialog
import com.development.nest.time.wrap.utils.renameDialog
import com.development.nest.time.wrap.utils.requestPermission
import com.development.nest.time.wrap.viewmodel.MainViewModel
import java.io.File

class FilesFragment : Fragment() {

    lateinit var binding: FragmentFilesBinding
    private var fragmentType = "images"
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilesBinding.inflate(layoutInflater, container, false)

        arguments?.let {
            fragmentType = it.getString("fragmentType", "images")
        }

        return binding.root
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val galleryAdapter = GalleryAdapter()

        val list = if (fragmentType == "images") {
            viewModel.loadFilesFromStorage(requireContext(), "images")
        } else {
            binding.noFoundIcon.setImageResource(R.drawable.no_video_found_icon)
            binding.noFoundTxt.text = getString(R.string.no_video_found)
            binding.noFoundDesTxt.text = getString(R.string.your_video_will_be_save_here)
            binding.saveBtnText.text = getString(R.string.warp_video_now)
            binding.modeIcon.setImageResource(R.drawable.video_cam)
            viewModel.loadFilesFromStorage(requireContext(), "videos")
        }
        viewModel.updatedList.observe(viewLifecycleOwner) {
            Log.d("listUpdate", "setupRecyclerView: $fragmentType")
            val list = if (fragmentType == "images") {
                viewModel.loadFilesFromStorage(requireContext(), "images")
            } else {
                binding.noFoundIcon.setImageResource(R.drawable.no_video_found_icon)
                viewModel.loadFilesFromStorage(requireContext(), "videos")
            }
            galleryAdapter.setList(list)
            if (list.isEmpty()) {
                binding.noItemFound.visibility = View.VISIBLE
            } else {
                binding.noItemFound.visibility = View.GONE
            }
        }

        galleryAdapter.setList(list)
        binding.filesList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = galleryAdapter
        }
        galleryAdapter.listener = object : AppEvents() {
            override fun onClick(file: File) {
                val bundle = Bundle()
                bundle.putString("file", file.absolutePath)
                navController.navigate(R.id.file_viewer_fragment, bundle)
            }

            override fun onIteMenuClick(file: File) {
                requireActivity().itemMenuBottomSheetDialog(file, {
                    requireActivity().renameDialog(file) {

                        val destination = File("${file.parent}/$it.${file.extension}")

                        copyFile(
                            requireActivity(), file.absolutePath, destination.absolutePath
                        ) { isCopied ->
                            if (isCopied) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    file.delete()
                                    viewModel.updatedList.postValue(true)
                                }, 100)
                            }
                        }

                    }
                }, {
                    requireActivity().confirmDeleteDialog {
                        file.delete()
                        viewModel.updatedList.postValue(true)
                    }
                })
            }
        }
        if (list.isEmpty()) {
            binding.noItemFound.visibility = View.VISIBLE
        }

        binding.warpPhotoNow.setOnClickListener {
            requireActivity().requestPermission(cameraPermission, requireActivity()) {
                val bundle = Bundle()
                bundle.putString("scanType", fragmentType)
                navController.navigate(R.id.camera_fragment, bundle)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })
    }
}