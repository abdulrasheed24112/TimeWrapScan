package com.development.nest.time.wrap.fragments

import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.FragmentCameraBinding
import com.development.nest.time.wrap.utils.AppEvents
import com.development.nest.time.wrap.utils.Constants.Companion.HORIZONTAL
import com.development.nest.time.wrap.utils.Constants.Companion.PICTURE
import com.development.nest.time.wrap.utils.Constants.Companion.VERTICAL
import com.development.nest.time.wrap.utils.Constants.Companion.VIDEO
import com.development.nest.time.wrap.utils.SharedPreferenceHelper
import com.development.nest.time.wrap.utils.SharedPreferenceHelper.BRIGHTNESS_SEEKBAR_PROGRESS
import com.development.nest.time.wrap.utils.SharedPreferenceHelper.SPEED_SEEKBAR_PROGRESS
import com.development.nest.time.wrap.utils.SharedPreferenceHelper.TIMER_TIME
import com.development.nest.time.wrap.utils.changeTextColor
import com.development.nest.time.wrap.utils.runOnUiThread
import com.development.nest.time.wrap.utils.safeClick
import com.development.nest.time.wrap.utils.scanningDiscardDialog
import com.development.nest.time.wrap.utils.startTimer
import com.development.nest.time.wrap.wrapingfilter.ViewRecorder
import com.development.nest.time.wrap.viewmodel.MainViewModel
import java.io.File
import java.io.IOException

class CameraFragment : Fragment() {


    lateinit var binding: FragmentCameraBinding
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }
    var isRecording = false
    private val viewModel: MainViewModel by activityViewModels()
    private var timer: CountDownTimer? = null
    var isTimerRunning = false

    companion object {
        var bitmap1: Bitmap? = null
    }

    private var scanType: String? = null
    var isPause = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(layoutInflater, container, false)

        scanType = arguments?.getString("scanType", null)
        Log.d("captureMode", "onCreateView: $scanType")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        initViews()
        eventListener()
    }

    private val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.cameraView.isScanning) {
                requireActivity().scanningDiscardDialog {
                    if (isRecording) {
                        stopRecording()
                        outputPath?.delete()
                    }
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    private fun eventListener() {
        binding.cameraView.listener = object : AppEvents() {
            override fun onCompleted() {
                runOnUiThread {
                    val direction =
                        if (SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE, PICTURE] == VIDEO) {
                            bitmap1 = null
                            CameraFragmentDirections.actionCameraToImageViewer(
                                null, outputPath?.absolutePath
                            )
                        } else {
                            CameraFragmentDirections.actionCameraToImageViewer(null, null)
                        }
                    navController.navigate(direction)
                    stopRecording()
                }
            }

            override fun onScanning() {
                usePixelCopy1(binding.cameraView)
            }
        }
    }

    private fun initViews() {
        Log.d("captureMode", "initViews captureMode: $scanType")
        Log.d("captureMode", "initViews scanType final: ${scanType == null}")
        val captureMode = if (!scanType.isNullOrEmpty()) {
            Log.d("captureMode", "initViews scanType not empty")
            if (scanType == "videos") VIDEO else PICTURE
        } else {
            SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE, PICTURE]
        }
        Log.d("captureMode", "initViews captureMode: $captureMode")

        if (captureMode == PICTURE) {
            binding.captureMode.setImageResource(R.drawable.camera_icon1)
            binding.startBtn.setImageResource(R.drawable.camera_capture_icon)
            SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE] = PICTURE
        } else {
            SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE] = VIDEO
            binding.captureMode.setImageResource(R.drawable.camera_icon)
            binding.startBtn.setImageResource(R.drawable.video_capture_icon)
        }
        if (SharedPreferenceHelper[SharedPreferenceHelper.ORIENTATION, HORIZONTAL] == HORIZONTAL) {
            binding.horizontalScan.changeTextColor(context, R.color.orange_color)
            binding.verticalScan.changeTextColor(context, R.color.white)
        } else {
            binding.verticalScan.changeTextColor(context, R.color.orange_color)
            binding.horizontalScan.changeTextColor(context, R.color.white)
        }

        binding.captureMode.setOnClickListener {
            if (SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE, PICTURE] == PICTURE) {
                SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE] = VIDEO
                binding.captureMode.setImageResource(R.drawable.camera_icon)
                binding.startBtn.setImageResource(R.drawable.video_capture_icon)
            } else {
                SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE] = PICTURE
                binding.captureMode.setImageResource(R.drawable.camera_icon1)
                binding.startBtn.setImageResource(R.drawable.camera_capture_icon)
            }
        }

        binding.crossIcon.setOnClickListener {
            navController.navigateUp()
        }
        binding.startBtn.setOnClickListener { view ->
            if (!binding.cameraView.isScanning) {
                binding.bottomControls.visibility = View.GONE
                binding.sideBar.visibility = View.GONE
                binding.crossIcon.visibility = View.GONE
            }
            val time = SharedPreferenceHelper[TIMER_TIME, 0]
            if (time == 0) {
                startScanning()
            } else {
                isTimerRunning = true
                binding.timerLay.visibility = View.VISIBLE
                timer = startTimer(binding.timeText, time) {
                    isTimerRunning = false
                    binding.timerLay.visibility = View.GONE
                    setTimerIcon(0)
                    startScanning()
                }
            }
        }
        binding.horizontalScan.setOnClickListener {
            SharedPreferenceHelper[SharedPreferenceHelper.ORIENTATION] = HORIZONTAL
            binding.horizontalScan.changeTextColor(context, R.color.orange_color)
            binding.verticalScan.changeTextColor(context, R.color.white)
        }
        binding.verticalScan.setOnClickListener {
            SharedPreferenceHelper[SharedPreferenceHelper.ORIENTATION] = VERTICAL
            binding.verticalScan.changeTextColor(context, R.color.orange_color)
            binding.horizontalScan.changeTextColor(context, R.color.white)
        }
        binding.switchCameraIcon.safeClick({
            if (SharedPreferenceHelper[SharedPreferenceHelper.CAMERA_DIRECTION, 0] == 0) {
                SharedPreferenceHelper[SharedPreferenceHelper.CAMERA_DIRECTION] = 1
            } else {
                SharedPreferenceHelper[SharedPreferenceHelper.CAMERA_DIRECTION] = 0
            }
            binding.cameraView.destroyCamera()
            Handler(Looper.getMainLooper()).postDelayed({
                ActivityCompat.recreate(requireActivity())
            }, 100)
        })

        //main sidebar
        binding.timer.setOnClickListener {
            binding.timerSideBar.visibility = View.VISIBLE
            binding.mainSideBar.visibility = View.GONE
        }
        binding.brightness.setOnClickListener {
            binding.brightnessSideBar.visibility = View.VISIBLE
            binding.mainSideBar.visibility = View.GONE
        }
        binding.speed.setOnClickListener {
            binding.speedSideBar.visibility = View.VISIBLE
            binding.mainSideBar.visibility = View.GONE
        }

        //speed bar
        val step = 1
        val max = 15
        val min = 2
        binding.speedSeekBar.max = (max - min) / step
        val speed = SharedPreferenceHelper[SPEED_SEEKBAR_PROGRESS, 10]
        binding.speedSeekBar.progress = speed
        binding.cameraView.speed = speed
        binding.speedText.text = "$speed${getString(R.string.sec)}"
        binding.speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val value = min + p1 * step
                SharedPreferenceHelper[SPEED_SEEKBAR_PROGRESS] = value
                binding.cameraView.speed = value
                binding.speedText.text = "$value${getString(R.string.sec)}"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        binding.speedBarCrossIcon.setOnClickListener {
            binding.speedSideBar.visibility = View.GONE
            binding.mainSideBar.visibility = View.VISIBLE
        }

        //brightness bar
        val brightnessProgress = SharedPreferenceHelper[BRIGHTNESS_SEEKBAR_PROGRESS, -1]
        binding.brightnessSeekBar.max = binding.cameraView.brightnessRange?.upper ?: 10
        if (brightnessProgress > -1) {
            binding.brightnessSeekBar.progress = brightnessProgress
        }

        binding.brightnessSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SharedPreferenceHelper[BRIGHTNESS_SEEKBAR_PROGRESS] = progress

                val minBrightness = binding.cameraView.brightnessRange?.lower ?: 0
                val maxBrightness = binding.cameraView.brightnessRange?.upper ?: 0

                Log.d("setBrightness", "progress: $progress")

                val brightness =
                    (progress - binding.brightnessSeekBar.max / 2) * ((maxBrightness - minBrightness) / binding.brightnessSeekBar.max) + (maxBrightness + minBrightness) / 2
                binding.cameraView.applyBrightnessLevel(brightness)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.brightnessCrossIcon.setOnClickListener {
            binding.brightnessSideBar.visibility = View.GONE
            binding.mainSideBar.visibility = View.VISIBLE
        }

        //timer bar
        setTimerIcon(SharedPreferenceHelper[TIMER_TIME, 0])
        binding.timerCrossIcon.setOnClickListener {
            binding.timerSideBar.visibility = View.GONE
            binding.mainSideBar.visibility = View.VISIBLE
        }
        binding.timer10Sec.setOnClickListener {
            setTimerIcon(10)
        }
        binding.timer5Sec.setOnClickListener {
            setTimerIcon(5)
        }
        binding.timer3Sec.setOnClickListener {
            setTimerIcon(3)
        }
        binding.timerOff.setOnClickListener {
            setTimerIcon(0)
        }

        //flashlight
        binding.flash.setOnClickListener {
            val isTorchOn = binding.cameraView.switchFlash()
            if (isTorchOn) {
                binding.flashIcon.setImageResource(R.drawable.flash_on_icon)
            } else {
                binding.flashIcon.setImageResource(R.drawable.flash_off)
            }
        }
    }

    private fun startScanning() {
        runOnUiThread {
            binding.cameraView.isScanning = !binding.cameraView.isScanning
            if (SharedPreferenceHelper[SharedPreferenceHelper.CAPTURE_MODE, PICTURE] == VIDEO) {
                initRecorder()
            }
        }
    }

    private fun stopScanning() {
        if (binding.cameraView.isScanning) {
            stopRecording()
            outputPath?.delete()
        }
        binding.cameraView.isScanning = false
        binding.bottomControls.visibility = View.VISIBLE
        binding.sideBar.visibility = View.VISIBLE
        binding.crossIcon.visibility = View.VISIBLE
    }


    override fun onPause() {
        super.onPause()
        isPause = true
        stopScanning()
//        binding.cameraView.destroyCamera()
    }

    override fun onResume() {
        super.onResume()
        if (isPause) {
            isPause = false
//            binding.cameraView.init()
//            binding.cameraView.openCamera2()
            navController.navigate(
                R.id.camera_fragment,
                arguments,
                NavOptions.Builder().setPopUpTo(R.id.camera_fragment, true).build()
            )
        }
//        requireActivity().recreate()
    }

    private fun setTimerIcon(time: Int) {
        SharedPreferenceHelper[TIMER_TIME] = time
        when (time) {
            10 -> {
                binding.timer10Sec.setImageResource(R.drawable.timer_10_on_icon)
                binding.timer5Sec.setImageResource(R.drawable.timer_5_icon)
                binding.timer3Sec.setImageResource(R.drawable.timer_3_icon)
                binding.timerOff.setImageResource(R.drawable.ic_timer)
                binding.mainTimerIcon.setImageResource(R.drawable.timer_10_on_icon)
            }
            5 -> {
                binding.timer10Sec.setImageResource(R.drawable.timer_10_icon)
                binding.timer5Sec.setImageResource(R.drawable.timer_5_on_icon)
                binding.timer3Sec.setImageResource(R.drawable.timer_3_icon)
                binding.timerOff.setImageResource(R.drawable.timer_off_off_icon)
                binding.mainTimerIcon.setImageResource(R.drawable.timer_5_on_icon)
            }
            3 -> {
                binding.timer10Sec.setImageResource(R.drawable.timer_10_icon)
                binding.timer5Sec.setImageResource(R.drawable.timer_5_icon)
                binding.timer3Sec.setImageResource(R.drawable.timer_3_on_icon)
                binding.timerOff.setImageResource(R.drawable.timer_off_off_icon)
                binding.mainTimerIcon.setImageResource(R.drawable.timer_3_on_icon)
            }
            else -> {
                binding.timer10Sec.setImageResource(R.drawable.timer_10_icon)
                binding.timer5Sec.setImageResource(R.drawable.timer_5_icon)
                binding.timer3Sec.setImageResource(R.drawable.timer_3_icon)
                binding.timerOff.setImageResource(R.drawable.timer_off_on_icon)
                binding.mainTimerIcon.setImageResource(R.drawable.timer_off_on_icon)
            }
        }

    }

    fun usePixelCopy1(videoView: SurfaceView) {
        val bitmap = Bitmap.createBitmap(videoView.width, videoView.height, Bitmap.Config.ARGB_8888)
        try {
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            PixelCopy.request(
                videoView, bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        runOnUiThread {
                            bitmap1 = bitmap
                            binding.ivCamera.setImageBitmap(bitmap)
                        }
                    }
                    handlerThread.quitSafely()
                }, Handler(handlerThread.looper)
            )
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        cancelTimer()
        SharedPreferenceHelper[TIMER_TIME] = 0
    }

    private fun cancelTimer() {
        if (isTimerRunning) {
            timer?.cancel()
        }
    }

    private var recorder: ViewRecorder? = null
    var outputPath: File? = null

    private fun initRecorder() {
        isRecording = true
        recorder = ViewRecorder()
        outputPath = viewModel.createInternalFile(context, "videos", ".mp4")

        //mViewRecorder.setAudioSource(MediaRecorder.AudioSource.MIC) // uncomment this line if audio required
        recorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        //mViewRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setVideoFrameRate(25) // 30fps
        recorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        recorder?.setVideoSize(720, 1080)
        recorder?.setVideoEncodingBitRate(2000 * 1000)
        recorder?.setOutputFile(outputPath?.absolutePath)
        recorder?.setRecordedView(binding.ivCamera)
        try {
            recorder?.prepare()
            recorder?.start()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            if (isRecording && recorder != null) {
                isRecording = false
                recorder?.stop()
                recorder?.release()
            }
        } catch (e: Exception) {
        }
    }
}
