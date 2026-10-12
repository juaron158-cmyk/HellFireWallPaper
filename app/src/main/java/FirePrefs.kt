package com.example.hellfirewallpaper

import android.content.Context

object FirePrefs {
    private const val NAME = "fire_prefs"
    private const val KEY_TOUCH = "touch_enabled"
    private const val KEY_GYRO = "gyro_enabled"
    private const val KEY_HUE = "hue"
    private const val KEY_DECAY = "decay"
    private const val KEY_IMAGE_URI = "image_uri"
    private const val KEY_FIT_MODE = "fit_mode"
    private const val KEY_CROP_SCALE = "crop_scale"
    private const val KEY_CROP_X = "crop_x"
    private const val KEY_WIND = "wind_f"
    private const val KEY_TICK_MS = "tick_ms"
    private const val KEY_SCALE = "scale"

    fun getScale(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_SCALE, 8)

    fun saveScale(context: Context, scale: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SCALE, scale).apply()
    }
    private const val KEY_CROP_Y = "crop_y"

    fun getHue(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HUE, 0)

    fun getDecay(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_DECAY, 125)

    fun getImageUri(context: Context): String? =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString(KEY_IMAGE_URI, null)

    fun getFitMode(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FIT_MODE, 1)

    fun getCropScale(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_CROP_SCALE, 1f)

    fun getCropX(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_CROP_X, 0f)

    fun getCropY(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_CROP_Y, 0f)

    fun save(context: Context, hue: Int, decay: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HUE, hue)
            .putInt(KEY_DECAY, decay)
            .apply()
    }

    fun saveImageUri(context: Context, uri: String?) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_IMAGE_URI, uri)
            .apply()
    }

    fun saveFitMode(context: Context, mode: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_FIT_MODE, mode)
            .apply()
    }

    fun saveCrop(context: Context, scale: Float, x: Float, y: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_CROP_SCALE, scale)
            .putFloat(KEY_CROP_X, x)
            .putFloat(KEY_CROP_Y, y)
            .apply()
    }
    fun getTouch(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_TOUCH, true)

    fun saveTouch(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TOUCH, enabled)
            .apply()
    }
    fun getWind(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat("wind_f", 0f)

    fun saveWind(context: Context, wind: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat("wind_f", wind)
            .apply()
    }

    fun getTickMs(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_TICK_MS, 50)

    fun saveTickMs(context: Context, ms: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TICK_MS, ms)
            .apply()
    }
    fun getGyro(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GYRO, false)

    fun saveGyro(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_GYRO, enabled)
            .apply()
    }
    private const val KEY_LANG = "lang" // "" = seguir sistema, "es", "en"

    fun getLang(context: Context): String =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "") ?: ""

    fun saveLang(context: Context, code: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, code).apply()
    }

}