package com.development.nest.time.wrap.ads

import android.app.Activity
import android.content.Context
import com.development.nest.time.wrap.utils.isNetworkAvailable

fun Activity.showInterstitial(onAdClosed: () -> Unit) {
    if (this.isNetworkAvailable()) {
        InterstitialClass.getInstance().showInterstitialAdNew(this) {
            onAdClosed.invoke()
        }
    } else {
        onAdClosed.invoke()
    }
}