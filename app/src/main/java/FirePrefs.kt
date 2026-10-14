package com.example.hellfirewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import kotlin.random.Random
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
    private const val KEY_CROP_Y = "crop_y"
    private const val KEY_WIND = "wind_f"
    private const val KEY_TICK_MS = "tick_ms"
    private const val KEY_SCALE = "scale"
    private const val KEY_LANG = "lang"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_TRANSPARENCY = "transparency"
    private const val KEY_SMOKE_ENABLED = "smoke_enabled"
    private const val KEY_SMOKE_DENSITY = "smoke_density"
    private const val KEY_LICHT_ENABLED = "licht_enabled"
    private const val KEY_LICHT_INTENSITY = "licht_intensity"
    private const val KEY_RAIN_LENGTH = "rain_length"
    private const val KEY_HELECHO_ENABLED = "helecho_enabled"
    private const val KEY_HELECHO_INTENSITY = "helecho_intensity"
    private const val KEY_RD_ENABLED = "rd_enabled"
    private const val KEY_RD_PRESET = "rd_preset"
    private const val KEY_RD_SPEED = "rd_speed"
    private const val KEY_SNOW_DENSITY = "snow_density"
    private lateinit var bitmap: Bitmap
    private lateinit var pixels: IntArray
    private var width = 0
    private var height = 0
    private var palette = IntArray(37) // 0 a 36
    private const val KEY_FROST_ENABLED = "frost_enabled"
    private const val KEY_FROST_SIZE = "frost_size"
    private const val KEY_MODE = "fire_mode"
    private const val KEY_FALL_SPEED = "fall_speed"
    fun getFrostSize(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_FROST_SIZE, 1f)

    fun saveFrostSize(context: Context, v: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_FROST_SIZE, v.coerceIn(0.3f, 2f)).apply()
    }
    fun getRDEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_RD_ENABLED, false)

    fun saveRDEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_RD_ENABLED, enabled).apply()
    }

    fun getRDPreset(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_RD_PRESET, 0)

    fun saveRDPreset(context: Context, preset: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_RD_PRESET, preset.coerceIn(0, 4)).apply()
    }
    fun getLichtEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LICHT_ENABLED, false)

    fun saveLichtEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LICHT_ENABLED, enabled).apply()
    }
    fun getRDSpeed(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_RD_SPEED, 1f)

    fun saveRDSpeed(context: Context, speed: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_RD_SPEED, speed.coerceIn(1f, 4f)).apply()
    }
    fun getLichtIntensity(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_LICHT_INTENSITY, 0.5f)

    fun saveLichtIntensity(context: Context, intensity: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_LICHT_INTENSITY, intensity.coerceIn(0f, 1f)).apply()
    }
    fun getRainLength(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_RAIN_LENGTH, 12f)
    fun getFrostEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(KEY_FROST_ENABLED, false)

    fun saveFrostEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_FROST_ENABLED, enabled).apply()
    }
    fun getHelechoEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HELECHO_ENABLED, false)

    fun saveHelechoEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_HELECHO_ENABLED, enabled).apply()
    }

    fun getHelechoIntensity(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_HELECHO_INTENSITY, 0.5f)

    fun saveHelechoIntensity(context: Context, intensity: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_HELECHO_INTENSITY, intensity.coerceIn(0f, 1f)).apply()
    }

    fun saveRainLength(context: Context, v: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_RAIN_LENGTH, v.coerceIn(4f, 30f)).apply()
    }

    fun getSnowDensity(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_SNOW_DENSITY, 0.5f)

    fun saveSnowDensity(context: Context, v: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_SNOW_DENSITY, v.coerceIn(0f, 1f)).apply()
    }
    fun getFallSpeed(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_FALL_SPEED, 1f)

    fun saveFallSpeed(context: Context, speed: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_FALL_SPEED, speed.coerceIn(0f, 2f)).apply()
    }
    private var lichtenbergSystem: MultiLichtenberg? = null

    fun setSize(w: Int, h: Int) {
        if (width == w && height == h) return
        width = w
        height = h
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        pixels = IntArray(w * h)

        // Inicializar sistema de Lichtenberg múltiple
        lichtenbergSystem = MultiLichtenberg(w, h, numRays = 4) // 4 rayos


    }
    fun getScale(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_SCALE, 8)

    fun saveScale(context: Context, scale: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SCALE, scale).apply()
    }

    fun getHue(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_HUE, 0)

    fun getDecay(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_DECAY, 125)

    fun getImageUri(context: Context): String? =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(KEY_IMAGE_URI, null)

    fun getFitMode(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_FIT_MODE, 1)

    fun getCropScale(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_CROP_SCALE, 1f)

    fun getCropX(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_CROP_X, 0f)

    fun getCropY(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_CROP_Y, 0f)

    fun save(context: Context, hue: Int, decay: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HUE, hue)
            .putInt(KEY_DECAY, decay)
            .apply()
    }

    fun saveImageUri(context: Context, uri: String?) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_IMAGE_URI, uri).apply()
    }

    fun saveFitMode(context: Context, mode: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FIT_MODE, mode).apply()
    }

    fun saveCrop(context: Context, scale: Float, x: Float, y: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_CROP_SCALE, scale)
            .putFloat(KEY_CROP_X, x)
            .putFloat(KEY_CROP_Y, y)
            .apply()
    }

    fun getTouch(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(KEY_TOUCH, true)

    fun saveTouch(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TOUCH, enabled).apply()
    }

    fun getWind(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_WIND, 0f)

    fun saveWind(context: Context, wind: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_WIND, wind).apply()
    }

    fun getTickMs(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_TICK_MS, 50)

    fun saveTickMs(context: Context, ms: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_TICK_MS, ms).apply()
    }

    fun getGyro(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(KEY_GYRO, false)

    fun saveGyro(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GYRO, enabled).apply()
    }

    fun getLang(context: Context): String =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(KEY_LANG, "") ?: ""

    fun saveLang(context: Context, code: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putString(KEY_LANG, code).apply()
    }


    fun getBrightness(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_BRIGHTNESS, 255)

    fun saveBrightness(context: Context, v: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BRIGHTNESS, v).apply()
    }

    fun getTransparency(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_TRANSPARENCY, 255)

    fun saveTransparency(context: Context, v: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_TRANSPARENCY, v).apply()
    }

    fun getSmokeEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(KEY_SMOKE_ENABLED, false)

    fun saveSmokeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SMOKE_ENABLED, enabled).apply()
    }

    fun getSmokeDensity(context: Context): Float =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(KEY_SMOKE_DENSITY, 0.5f)

    fun saveSmokeDensity(context: Context, density: Float) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_SMOKE_DENSITY, density.coerceIn(0f, 1f)).apply()
    }

    fun getMode(context: Context): Int =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_MODE, 0)

    fun saveMode(context: Context, mode: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_MODE, mode.coerceIn(0, 2)).apply()
    }

}