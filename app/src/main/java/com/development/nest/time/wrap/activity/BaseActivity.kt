package com.development.nest.time.wrap.activity

import android.app.LocaleManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.development.nest.time.wrap.utils.SharedPreferenceHelper

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(contex: Context) {
        SharedPreferenceHelper.init(contex)
//
//        // Get the language code from your preferred storage (e.g., SharedPreferences)
        val languageCode = SharedPreferenceHelper[SharedPreferenceHelper.SELECTED_LANGUAGE, "en"]
//
//        // Set the new locale for the base context
        val newLocaleContext = SharedPreferenceHelper.setLocale(contex, languageCode)

        super.attachBaseContext(newLocaleContext)
    }
}
