package com.development.nest.time.wrap.wrapingfilter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLES20
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.PermissionChecker
import com.development.nest.time.wrap.utils.AppEvents
import com.development.nest.time.wrap.utils.SharedPreferenceHelper
import com.development.nest.time.wrap.utils.getPreviewOutputSize
import java.util.*
import kotlin.math.roundToInt


class Camera2SurfaceView : SurfaceView {

    private lateinit var mEglUtils: EGLUtils
    private lateinit var videoRenderer: GLVideoRenderer
    private lateinit var mRenderer: GLRenderer
    private lateinit var scanRenderer: GLScanRenderer
    private var mCameraId: String? = null
    private var mCameraManager: CameraManager? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mHandler: Handler? = null
    private var screenWidth = -1
    private var screenHeight = 0
    var previewWidth = 0
    var previewHeight = 0
    private val rect = Rect()
    private var cameraHandler: Handler? = null
    private var cameraThread: HandlerThread? = null
    var isScanning = false
    private var isf = false
    private var scanHeight = 0f
    private var pixelHeight = 0f
    var speed = 15
    var listener: AppEvents? = null
    var builder: CaptureRequest.Builder? = null
    private val cameraBack = "0"
    private var isFlashSupported = true
    private var isTorchOn = false
    private var aspectRatio = 0f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
        holder.setFixedSize(width, height)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height)
        } else {
            // Performs center-crop transformation of the camera frames
            val newWidth: Int
            val newHeight: Int
            val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio
            if (width < height * actualRatio) {
                newHeight = height
                newWidth = (height * actualRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / actualRatio).roundToInt()
            }

            Log.d(TAG, "Measured dimensions set: $newWidth x $newHeight")
            setMeasuredDimension(newWidth, newHeight)
        }
    }

    companion object {
        private val TAG = AutoFitSurfaceView::class.java.simpleName
    }

    private val characteristics: CameraCharacteristics? by lazy {
        mCameraId?.let { mCameraManager?.getCameraCharacteristics(it) }
    }

    fun init() {
        mEglUtils = EGLUtils()
        videoRenderer =
            GLVideoRenderer()
        mRenderer = GLRenderer()
        scanRenderer = GLScanRenderer()

        cameraThread = HandlerThread("Camera2Thread")
        cameraThread?.start()
        cameraHandler = Handler(cameraThread!!.looper)
        initCamera2()

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                val mPreviewSize = characteristics?.let {
                    getPreviewOutputSize(
                        display, it, SurfaceHolder::class.java
                    )
                }
                previewWidth = mPreviewSize?.height ?: 0
                previewHeight = mPreviewSize?.width ?: 0
                surfaceHolder.setFixedSize(previewWidth, previewHeight)
                Log.d("previewSizee", "preview: $previewWidth:$previewHeight")

                cameraHandler?.post {
                    mEglUtils.initEGL(holder.surface)
                    GLES20.glEnable(GLES20.GL_BLEND)
                    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                    mRenderer.initShader()
                    videoRenderer.initShader()
                    scanRenderer.initShader()
                    videoRenderer.setOnFrameAvailableListener { surfaceTexture: SurfaceTexture? ->
                        cameraHandler?.post {
                            if (mCameraCaptureSession == null) {
                                return@post
                            }
                            videoRenderer.drawFrame()
                            var videoTexture = videoRenderer.texture
                            if (isScanning) {
                                Log.d("onFrameAvailable", "is scanning")
                                if (!isf) {
                                    scanHeight = pixelHeight * speed
                                } else {
                                    scanHeight += pixelHeight * speed
                                }
                                if (scanHeight < 2.0) {
                                    var fh = scanHeight
                                    if (scanHeight >= 1.0) {
                                        scanHeight = 3.0f
                                        fh = 1.0f
                                    }
                                    Log.d("onFrameAvailable", "is drawing frame")
                                    listener?.onScanning()
                                    scanRenderer.drawFrame(videoRenderer.texture, fh, speed)
                                } else {
                                    isScanning = false
                                    listener?.onCompleted()
                                    Log.d("onFrameAvailable", "is not drawing frame")
                                }
                                videoTexture = scanRenderer.texture
                            }
                            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
                            Log.d("viewPortRect", "surfaceCreated: $rect")
                            Log.d("viewPortRect", "screen: $screenWidth:$screenHeight")
                            Log.d("viewPortRect", "preview: $previewWidth:$previewHeight")

                            GLES20.glViewport(0, rect.top, previewWidth, previewHeight)
//                            GLES20.glViewport(0, 0, previewWidth, previewHeight)
                            mRenderer.drawFrame(videoTexture)
                            isf = isScanning
                            mEglUtils.swap()
                        }
                    }
                    if (screenWidth != -1) {
                        openCamera2()
                    }
                }
            }

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, w: Int, h: Int) {
                val sw = screenWidth
                screenWidth = w
                screenHeight = h
                Log.d("viewPortRect", "screen: $w:$h")
                cameraHandler?.post {
                    display?.let {
                        val mPreviewSize = characteristics?.let {
                            getPreviewOutputSize(
                                display, it, SurfaceHolder::class.java
                            )
                        }
                        previewWidth = mPreviewSize?.height ?: 0
                        previewHeight = mPreviewSize?.width ?: 0
                        pixelHeight = 1.0f / previewHeight
                        val left: Int
                        val top: Int
                        val viewWidth: Int
                        val viewHeight: Int
                        val sh = screenWidth * 1.0f / screenHeight
                        val vh = previewWidth * 1.0f / previewHeight
                        if (sh < vh) {
                            left = 0
                            viewWidth = screenWidth
                            viewHeight = (previewHeight * 1.0f / previewWidth * viewWidth).toInt()
                            top = (screenHeight - viewHeight) / 2
                        } else {
                            top = 0
                            viewHeight = screenHeight
                            viewWidth = (previewWidth * 1.0f / previewHeight * viewHeight).toInt()
                            left = (screenWidth - viewWidth) / 2
                        }
                        rect.left = left
                        rect.top = top
                        rect.right = left + viewWidth
                        rect.bottom = top + viewHeight
                        Log.d("mPreviewSize", "mPreviewSize viewWidth: $viewWidth")
                        Log.d("mPreviewSize", "mPreviewSize viewHeight: $viewHeight")
                        Log.d("mPreviewSize", "mPreviewSize width: ${mPreviewSize?.width}")
                        Log.d("mPreviewSize", "mPreviewSize height: ${mPreviewSize?.height}")
//                    videoRenderer.setSize(mPreviewSize!!.width, mPreviewSize.height)
//                    scanRenderer.setSize(mPreviewSize.width, mPreviewSize.height)
                        mPreviewSize?.height?.let {
                            videoRenderer.setSize(
                                mPreviewSize.width, it
                            )
                        }
                        mPreviewSize?.height?.let {
                            scanRenderer.setSize(
                                mPreviewSize.width, it
                            )
                        }
                        if (sw == -1) {
                            openCamera2()
                        }
                    }
                }
            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                destroyCamera()
            }
        })
    }

    fun destroyCamera() {
        cameraHandler?.post {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession?.device?.close()
                mCameraCaptureSession?.close()
                mCameraCaptureSession = null
            }
            GLES20.glDisable(GLES20.GL_BLEND)
            videoRenderer.release()
            mRenderer.release()
            scanRenderer.release()
            mEglUtils.release()
        }
    }

    private var mSizes: Array<Size>? = null
    var cameraIdList: Array<out String>? = null

    private fun initCamera2() {
        val handlerThread = HandlerThread("Camera2")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            assert(mCameraManager != null)
            cameraIdList = mCameraManager?.cameraIdList
            cameraIdList?.let {
                mCameraId = it[SharedPreferenceHelper[SharedPreferenceHelper.CAMERA_DIRECTION, 0]]
                val characteristics = mCameraManager?.getCameraCharacteristics(
                    it[SharedPreferenceHelper[SharedPreferenceHelper.CAMERA_DIRECTION, 0]]
                )
                characteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val map =
                    characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                if (map != null) {
                    mSizes = map.getOutputSizes(SurfaceTexture::class.java)
                }
            }
        } catch (e: CameraAccessException) {
            Log.d("cameraException", "initCamera2: $e")
        }
    }

    @SuppressLint("WrongConstant")
    fun openCamera2() {
        if (PermissionChecker.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                mCameraId?.let {
                    mCameraManager?.openCamera(it, stateCallback, mHandler)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            takePreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            if (mCameraDevice != null) {
                mCameraDevice?.close()
                mCameraDevice = null
            }
        }

        override fun onError(cameraDevice: CameraDevice, i: Int) {}
    }


    private fun takePreview() {
        try {
            builder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            builder?.addTarget(videoRenderer.surface)

            mCameraDevice?.createCaptureSession(
                listOf(videoRenderer.surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (null == mCameraDevice) return
                        mCameraCaptureSession = cameraCaptureSession
                        builder?.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        builder?.set(
                            CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                        )

                        try {
                            val minBrightness = brightnessRange?.lower ?: 0
                            val maxBrightness = brightnessRange?.upper ?: 0
                            val brightnessProgress =
                                SharedPreferenceHelper[SharedPreferenceHelper.BRIGHTNESS_SEEKBAR_PROGRESS, -1]
                            if (brightnessProgress > -1) {
                                val brightness =
                                    (brightnessProgress - maxBrightness / 2) * ((maxBrightness - minBrightness) / maxBrightness) + (maxBrightness + minBrightness) / 2
                                builder?.set(
                                    CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, brightness
                                )
                            }
                        } catch (e: Exception) {

                        }

                        val previewRequest = builder?.build()
                        try {
                            previewRequest?.let {
                                mCameraCaptureSession?.setRepeatingRequest(
                                    it, null, mHandler
                                )
                            }
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
                }, mHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun switchFlash(): Boolean {
        val available = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        isFlashSupported = available ?: false

        try {
            if (mCameraId.equals(cameraBack)) {
                if (isFlashSupported) {
                    if (!isTorchOn) {
                        isTorchOn = true
                        builder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                        builder?.set(
                            CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON
                        )
                    } else {
                        isTorchOn = false
                        builder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
//                        builder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                    }
                }
                builder?.build()?.let { mCameraCaptureSession?.setRepeatingRequest(it, null, null) }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return isTorchOn
    }

    val brightnessRange by lazy { characteristics?.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE) }

    fun applyBrightnessLevel(brightness: Int) {
        try {
            builder?.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, brightness)
            builder?.build()?.let { mCameraCaptureSession?.setRepeatingRequest(it, null, null) }
        } catch (e: CameraAccessException) {
//            Log.e(TAG, "Failed to apply brightness level: " + e.message)
        }
    }

    private fun getPreferredPreviewSize(sizes: Array<Size>, width: Int, height: Int): Size {
        val collectorSizes: MutableList<Size> = ArrayList()
        for (option in sizes) {
            if (width > height) {
                if (option.width > width && option.height > height) {
                    collectorSizes.add(option)
                }
            } else {
                if (option.height > width && option.width > height) {
                    collectorSizes.add(option)
                }
            }
        }
        return if (collectorSizes.size > 0) {
            Collections.min(collectorSizes) { s1: Size, s2: Size -> java.lang.Long.signum((s1.width * s1.height - s2.width * s2.height).toLong()) }
        } else sizes[0]
    }

}