package com.development.nest.time.wrap.activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.ActivityPermissionBinding
import com.development.nest.time.wrap.utils.allPermissions
import com.development.nest.time.wrap.utils.fullScreen
import com.development.nest.time.wrap.utils.requestPermission

class PermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.let {
            fullScreen(window)
        }

        if (Build.VERSION.SDK_INT >= 33) {
//            binding.permissionImg.setImageResource(R.drawable.img_notification_permission)
            binding.permDesc.text = getString(R.string.access_to_your_camera_and_allows_you)
        }

        binding.allowBtn.setOnClickListener {
            requestPermission(allPermissions, this) {
                gotoActivity()
            }
        }
        binding.notAllowBtn.setOnClickListener {
            gotoActivity()
        }
    }

    private fun gotoActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}