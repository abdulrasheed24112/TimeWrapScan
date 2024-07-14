package com.development.nest.time.wrap.ads


import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.ads.OpenApp.Companion.isInterstitialShown
import com.development.nest.time.wrap.utils.isInternetOn
import com.development.nest.time.wrap.utils.staticDialog
import com.development.nest.time.wrap.utils.showLoadingDialog
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialClass {

    var mInterstitialAd: InterstitialAd? = null
        private set

    companion object {
        @Volatile
        private var instance: InterstitialClass? = null
        var onCloseCallback: (() -> Unit)? = null
        var counter = 0
        var isAdShown = false
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: InterstitialClass().also {
                instance = it
            }
        }

        var delayForNextAd = 0L
    }


    fun loadInterstitialAd(context: Context) {
        if (isInternetOn(context)) {
            context.let {
                val interId = it.getString(R.string.admob_interistitial)
                InterstitialAd.load(it,
                    interId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(ad: LoadAdError) {
                            isInterstitialShown = false
                            if (counter == 2) {
                                onCloseCallback?.invoke()
                            } else {
                                counter++
                                onCloseCallback?.invoke()
                                Log.d("loaded_interstitial", "onAdFailedToLoad ${ad.code}")
                            }
                            isAdShown = false
                            staticDialog?.dismiss()
                        }

                        override fun onAdLoaded(ad: InterstitialAd) {
                            mInterstitialAd = ad
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        onCloseCallback?.invoke()
                                        isInterstitialShown = false
                                        mInterstitialAd = null
                                        isAdShown = false
                                        staticDialog?.dismiss()
                                        loadInterstitialAd(context)

                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        isInterstitialShown = false
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        onCloseCallback?.invoke()
                                        isAdShown = false
                                        staticDialog?.dismiss()
                                        loadInterstitialAd(context)

                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        isInterstitialShown = true
                                        super.onAdShowedFullScreenContent()
                                        isAdShown = false

                                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                                            { staticDialog?.dismiss() }, 200
                                        )
                                    }
                                }
                        }
                    })
            }
        }

    }

    fun showInterstitialAdNew(activity: Activity, onActionPerformed: (() -> Unit)? = null) {
        if (mInterstitialAd != null) {

            if (!isAdShown) {
                staticDialog = activity.showLoadingDialog()
                activity.let {
                    android.os.Handler(activity.mainLooper).postDelayed({
                        activity.let {
                            mInterstitialAd?.show(it)
                        }
                    }, 1000)
                }
            }
            onCloseCallback = {
                onActionPerformed?.invoke()
                onCloseCallback = null
            }

        } else {
            onActionPerformed?.invoke()
        }

    }

}
