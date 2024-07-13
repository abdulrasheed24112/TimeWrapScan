package com.development.nest.time.wrap.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.FragmentFileViewBinding
import com.development.nest.time.wrap.utils.confirmDeleteDialog
import com.development.nest.time.wrap.utils.copyFile
import com.development.nest.time.wrap.utils.isFileSaved
import com.development.nest.time.wrap.utils.requestPermission
import com.development.nest.time.wrap.utils.safeClick
import com.development.nest.time.wrap.utils.shareFile
import com.development.nest.time.wrap.utils.shareOnSocialMedia
import com.development.nest.time.wrap.utils.showToast
import com.development.nest.time.wrap.utils.storagePermissions
import com.development.nest.time.wrap.viewmodel.MainViewModel
import java.io.File

class FileViewFragment : Fragment() {
    lateinit var binding: FragmentFileViewBinding
    var file: File? = null
    var isVideo = false
    private val viewModel: MainViewModel by activityViewModels()
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            it.getString("file")?.let { path ->
                file = File(path)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        file?.let { path ->
            binding.tvFileName.text = path.nameWithoutExtension
            if (path.extension == "jpg") {
                Glide.with(binding.previewImage.context).load(file).into(binding.previewImage)
            } else {
                isVideo = true
                binding.videoView.visibility = View.VISIBLE

//                val surfaceholder: SurfaceHolder = binding.videoView.holder
//                surfaceholder.setFormat(PixelFormat.TRANSPARENT)

//                val mRetriever = MediaMetadataRetriever()
//                mRetriever.setDataSource(path.path)
//                val frame = mRetriever.frameAtTime
//                val width = frame!!.width
//                val height = frame.height
//                Log.d("videoView", "onViewCreated width: $width height: $height")

                binding.videoView.setVideoURI(Uri.parse(path.absolutePath))
//                binding.videoView.setBackgroundColor(Color.WHITE)
//                binding.videoView.layoutParams = FrameLayout.LayoutParams(550, 550)
//                val metrics = DisplayMetrics()
//                requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
//                binding.videoView.layoutParams = RelativeLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels)


                binding.videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
//                    binding.videoView.setZOrderOnTop(true)
//                    binding.videoView.setBackgroundColor(Color.WHITE); // Your color.

//                    Log.d("videoView", "onViewCreated width: ${binding.videoView.width} height: ${binding.videoView.height}")

                    /*val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
                    val screenRatio = binding.videoView.width / binding.videoView.height.toFloat()
                    val scaleX = videoRatio / screenRatio
                    if (scaleX >= 1f) {
                        binding.videoView.scaleX = scaleX
                    } else {
                        binding.videoView.scaleY = 1f / scaleX
                    }*/
                }
            }
            binding.videoView.start()

            if (isFileSaved(path)) {
                binding.exportToGalleryBtn.visibility = View.GONE
                binding.shareLay.visibility = View.VISIBLE
            }

            binding.shareIcon.safeClick({
                requireActivity().shareFile(path.absolutePath)
            }, 1000)
            binding.deleteIcon.setOnClickListener {
                requireActivity().confirmDeleteDialog {
                    file?.delete()
                    viewModel.updatedList.postValue(true)
                    navController.navigateUp()
                }
            }
            binding.cvBack.setOnClickListener {
                navController.navigateUp()
            }
            binding.exportToGalleryBtn.setOnClickListener {
                requireActivity().requestPermission(storagePermissions, requireActivity()) {
                    exportToGallery()
                }
            }

            binding.fbShare.safeClick({
                context?.let { it1 ->
                    shareOnSocialMedia(
                        it1, path.absolutePath, "com.facebook.katana"
                    )
                }
            }, 1000)
            binding.waShare.safeClick({
                context?.let { it1 ->
                    shareOnSocialMedia(
                        it1, path.absolutePath, "com.whatsapp"
                    )
                }
            }, 1000)

            binding.twitterShare.safeClick({
                context?.let { it1 ->
                    shareOnSocialMedia(
                        it1, path.absolutePath, "com.twitter.android"
                    )
                }
            }, 1000)
            binding.allShare.safeClick({ requireActivity().shareFile(path.absolutePath) }, 1000)
        }
    }

    private fun exportToGallery() {
        file?.let {
            if (!isFileSaved(it)) {
                val fileType = it.extension
                val externalPicturesDir = if (fileType == "jpg") {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                } else {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                }
                val directory = File("$externalPicturesDir/Time Warp Scan")
                if (!directory.exists()) {
                    directory.mkdir()
                }
                val destination = "$directory/${it.name}"
                copyFile(requireActivity(), it.absolutePath, destination) {}
                requireActivity().showToast(getString(R.string.exported_successful))
                binding.exportToGalleryBtn.visibility = View.GONE
                binding.shareLay.visibility = View.VISIBLE
            } else {
                requireActivity().showToast(getString(R.string.already_saved))
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

    override fun onPause() {
        super.onPause()
        binding.videoView.suspend()
    }

    override fun onResume() {
        super.onResume()
        binding.videoView.resume()
    }

}