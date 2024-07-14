package com.development.nest.time.wrap.utils

import android.app.Application
import com.development.nest.time.wrap.ads.InterstitialClass
import com.development.nest.time.wrap.ads.OpenApp
import com.google.firebase.FirebaseApp

class AppClass : Application() {
    private var appOpenAd: OpenApp? = null
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        appOpenAd = OpenApp(this)
        InterstitialClass.getInstance().loadInterstitialAd(this)
    }

    override fun onTerminate() {
        appOpenAd = null
        super.onTerminate()
    }
}