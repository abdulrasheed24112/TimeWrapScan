package com.development.nest.time.wrap.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.ads.NativeAdsClass
import com.development.nest.time.wrap.ads.showInterstitial
import com.development.nest.time.wrap.databinding.ActivitySplashBinding
import com.development.nest.time.wrap.utils.isNetworkAvailable


class SplashActivity : BaseActivity() {

    private lateinit var activitySplashBinding: ActivitySplashBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)


        if (isNetworkAvailable()) {
            Handler(Looper.getMainLooper()).postDelayed({
                activitySplashBinding.progressBar.visibility = View.GONE
                activitySplashBinding.btnStarted.visibility = View.VISIBLE
            }, 6000)
        } else {
            activitySplashBinding.progressBar.visibility = View.GONE
            activitySplashBinding.btnStarted.visibility = View.VISIBLE
        }


        val requiredPermissions = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val animation = TranslateAnimation(10f, 320f, 0f, 0f)
        animation.duration = 10000
        animation.fillAfter = true
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })

        activitySplashBinding.line.startAnimation(animation)


        if (isNetworkAvailable()) {
            NativeAdsClass(this@SplashActivity).setNativeAdView(
                activitySplashBinding.nativeAdLayout.rootLayout,
                activitySplashBinding.nativeAdLayout.splashShimmer,
                activitySplashBinding.nativeAdLayout.nativeAdContainerView,
                R.layout.small_native_ad,
                getString(R.string.splash_native)
            )
        } else {
            activitySplashBinding.nativeAdLayout.root.visibility = View.GONE
        }
        activitySplashBinding.btnStarted.setOnClickListener {
            showInterstitial {
                if (!hasPermissions(requiredPermissions)) {
                    startActivity(Intent(this, PermissionActivity::class.java))

                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}