package com.development.nest.time.wrap.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object SharedPreferenceHelper {

    private const val APP_PREFERENCES = "app_preferences"
    const val CAMERA_DIRECTION = "CAMERA_DIRECTION"
    const val CAPTURE_MODE = "CAPTURE_MODE"
    const val ORIENTATION = "ORIENTATION"
    const val SPEED_SEEKBAR_PROGRESS = "SPEED_SEEKBAR_PROGRESS"
    const val BRIGHTNESS_SEEKBAR_PROGRESS = "BRIGHTNESS_SEEKBAR_PROGRESS"
    const val TIMER_TIME = "TIMER_TIME"
    const val SELECTED_LANGUAGE = "SELECTED_LANGUAGE"

    lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) {
        preferences.edit { editor ->
            when (value) {
                is String? -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Long -> editor.putLong(key, value)
                else -> throw UnsupportedOperationException("Not yet implemented")
            }
        }
    }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T =
        when (T::class) {
            String::class -> preferences.getString(key, defaultValue as String? ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

}