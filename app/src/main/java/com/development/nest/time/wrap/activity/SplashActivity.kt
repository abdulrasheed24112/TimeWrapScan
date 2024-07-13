package com.development.nest.time.wrap.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.development.nest.time.wrap.databinding.ActivitySplashBinding


class SplashActivity : BaseActivity() {

    lateinit var activitySplashBinding: ActivitySplashBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)

        val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val animation = TranslateAnimation(10f, 320f, 0f, 0f)
        animation.duration = 10000 // Duration in milliseconds
        animation.fillAfter = true // Keep the animation's end position
        animation.repeatCount = Animation.INFINITE // Repeat animation indefinitely
        animation.repeatMode= Animation.REVERSE
        // Set the animation listener if needed
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // Code to run when animation starts
            }

            override fun onAnimationEnd(animation: Animation) {
                // Code to run when animation ends
            }

            override fun onAnimationRepeat(animation: Animation) {
                // Code to run when animation repeats
            }
        })

        // Start the animation
        activitySplashBinding.line.startAnimation(animation)



        activitySplashBinding.btnStarted.setOnClickListener {
            if (!hasPermissions(requiredPermissions)) {
                startActivity(Intent(this, PermissionActivity::class.java))

            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
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