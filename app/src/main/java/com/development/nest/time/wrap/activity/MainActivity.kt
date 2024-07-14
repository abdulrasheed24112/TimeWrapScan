package com.development.nest.time.wrap.activity

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.ActivityMainBinding
import com.development.nest.time.wrap.utils.SharedPreferenceHelper
import com.development.nest.time.wrap.utils.allPermissions
import com.development.nest.time.wrap.utils.checkPermissions
import com.development.nest.time.wrap.utils.fullScreen
import com.development.nest.time.wrap.utils.requestPermission
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private var isStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.let {
            fullScreen(window)
        }

        setupFirebaseMessaging()

        isStart = savedInstanceState?.getBoolean("isStart") ?: true
        Log.d("***TAG", "onCreate isStart: $isStart")

        initializeNavController()


    }

    private fun setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("***TAG", "onCreate: $token")
            } else {
                Log.w("***TAG", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun initializeNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun handleStartLogic() {
        isStart = false

        if (SharedPreferenceHelper["isShowPremium", false] && !checkPermissions(allPermissions)) {
            requestPermission(allPermissions, this) {
                // Handle permission granted logic if needed
            }
        }

        if (!SharedPreferenceHelper["isShowPremium", false]) {
            SharedPreferenceHelper["isShowPremium"] = true
            navController?.navigate(R.id.premium_fragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("isStart", "onSaveInstanceState isStart: $isStart")
        outState.putBoolean("isStart", isStart)
        super.onSaveInstanceState(outState)
    }
}