package com.development.nest.time.wrap.ads

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.utils.NATIVE_CTA_COLOR
import com.development.nest.time.wrap.utils.isNetworkAvailable
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView


class NativeAdsClass(private val activity: Context) {
    private var nativeAd: NativeAd? = null
    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.starRatingView = adView.findViewById(R.id.ratingBar)
        //adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.bodyView = adView.findViewById(R.id.ad_advertiser)
        try {
            adView.callToActionView?.setBackgroundColor()
            adView.findViewById<TextView>(R.id.adLabel).setBackgroundColor()
        } catch (e: Exception) {
            Log.d("populateUnifiedNativeAdView", "populateUnifiedNativeAdView: ${e.message}")
        }

        adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        if (adView.mediaView != null) {
            //adView.getMediaView().setVisibility(View.VISIBLE);
        } else {
            adView.mediaView?.visibility = View.GONE
        }

        if (nativeAd.headline != null) (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        if (nativeAd.icon != null) {
            adView.iconView?.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        } else adView.iconView?.visibility = View.GONE

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        }

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.GONE
        } else {
            (adView.bodyView as TextView).text = nativeAd.body
            adView.bodyView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    @JvmOverloads
    fun setNativeAdView(
        adLayout: ConstraintLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        frameLayout: FrameLayout, layoutId: Int,
        addUnitId: String,
        onFail: ((String?) -> Unit)? = null,
        onLoad: ((NativeAd?) -> Unit)? = null,

        ) = if (activity.isNetworkAvailable()) {
        frameLayout.removeAllViews()
        val builder = AdLoader.Builder(
            activity, addUnitId
        )
        builder.forNativeAd { unifiedNativeAd: NativeAd ->
            if (nativeAd != null) {
                nativeAd!!.destroy()
            }
            nativeAd = unifiedNativeAd
            val adView =
                (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    layoutId, null
                ) as NativeAdView
            populateUnifiedNativeAdView(unifiedNativeAd, adView)
            frameLayout.removeAllViews()
            frameLayout.addView(adView)
        }
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                shimmerFrameLayout.visibility = View.VISIBLE
                shimmerFrameLayout.startShimmer()
                super.onAdFailedToLoad(loadAdError)
                nativeAd?.let {
                    shimmerFrameLayout.visibility = View.VISIBLE
                    shimmerFrameLayout.startShimmer()
                    onFail?.invoke(loadAdError.message)
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE

                onLoad?.invoke(nativeAd!!)
            }
        }).withNativeAdOptions(adOptions).build()
        adLoader.loadAd(AdRequest.Builder().build())
    } else {
        frameLayout.visibility = View.GONE
        adLayout.visibility = View.GONE
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
    }

    private fun View.setBackgroundColor() {
        when (val viewBackground: Drawable = background) {
            is ShapeDrawable -> viewBackground.paint.color = Color.parseColor(NATIVE_CTA_COLOR)
            is GradientDrawable -> viewBackground.setColor(Color.parseColor(NATIVE_CTA_COLOR))
            is ColorDrawable -> viewBackground.color = Color.parseColor(NATIVE_CTA_COLOR)
        }
    }

}