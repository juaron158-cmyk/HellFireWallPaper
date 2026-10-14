package com.example.hellfirewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.max

class FireWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = FireEngine()

    inner class FireEngine : Engine() {
        private var helecho: FractalHelecho? = null
        private var helechoBmp: Bitmap? = null
        private var licht: Lichtenberg? = null
        private var lichtBmp: Bitmap? = null
        private val lichtPaint = Paint().apply { isFilterBitmap = false }

        private var helechoPaint = Paint().apply { isFilterBitmap = false }
        private var scale = 8
        private var fallSpeed = 1.0f

        private var sim: FireSimulation? = null
        private var fireBmp: Bitmap? = null
        private var smokeBmp: Bitmap? = null
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        // Paints reutilizados (no se crean en cada frame)
        private val firePaint = Paint().apply { isFilterBitmap = false }
        private val smokePaint = Paint().apply { isFilterBitmap = false }

        private var touchEnabled = true
        private var tickMs = 50L
        private var hue = 0
        private var decay = 1.25f
        private var wind = 0f
        private var brightness = 1f
        private var rd: DielectricGrowth? = null
        private var rdBmp: Bitmap? = null
        private val rdPaint = Paint().apply { isFilterBitmap = false }
        private var transparency = 1f
        private var burstFrames = 0
        private var frost: FrostLayer? = null
        private var frostBmp: Bitmap? = null
        private var frostEnabled = false
        private val frostPaint = Paint().apply { isFilterBitmap = false }
        private var cropScale = 1f
        private var cropX = 0f
        private var cropY = 0f

        private var bgBitmap: Bitmap? = null
        private var lastUri: String? = null

        private var pile: SnowPile? = null
        private var pileBmp: Bitmap? = null
        private val pilePaint = Paint().apply { isFilterBitmap = false }
        private val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private var motionListener: SensorEventListener? = null
        private var sensorActive = false


        private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            handler.post { loadPrefs() }
        }

        private val tick = object : Runnable {
            override fun run() {
                if (surfaceHolder.surface?.isValid == true) {
                    sim?.step()
                    sim?.let { s -> pile?.step(s) }
                    if (frostEnabled) frost?.step(sim?.impact ?: 0f)
                    helecho?.step()
                    rd?.step()
                    licht?.step()
                    draw()
                }
                handler.postDelayed(this, if (visible) tickMs else tickMs * 10)
            }
        }

        init {
            loadPrefs()
        }

        private fun loadPrefs() {
            val ctx: Context = this@FireWallpaperService
            hue = FirePrefs.getHue(ctx)
            decay = FirePrefs.getDecay(ctx) / 100f
            wind = if (FirePrefs.getGyro(ctx)) 0f else FirePrefs.getWind(ctx)
            touchEnabled = FirePrefs.getTouch(ctx)
            tickMs = FirePrefs.getTickMs(ctx).toLong()
            pile?.enabled = FirePrefs.getMode(ctx) == 2
            cropScale = FirePrefs.getCropScale(ctx)
            cropX = FirePrefs.getCropX(ctx)
            cropY = FirePrefs.getCropY(ctx)
        pile?.enabled = FirePrefs.getMode(ctx) == 2   // solo en modo nieve

            frost?.size = FirePrefs.getFrostSize(ctx)
            brightness = FirePrefs.getBrightness(ctx) / 255f
            fallSpeed = FirePrefs.getFallSpeed(ctx).toFloat()
            transparency = FirePrefs.getTransparency(ctx) / 255f

            if (FirePrefs.getGyro(ctx)) registerSensors() else unregisterSensors()

            sim?.let {
                it.hue = hue
                it.decayFactor = decay
                it.wind = wind
                it.brightness = brightness
                it.rainLength = FirePrefs.getRainLength(ctx)
                it.snowDensity = FirePrefs.getSnowDensity(ctx)
                it.fallSpeed = FirePrefs.getFallSpeed(ctx)
                frostEnabled = FirePrefs.getFrostEnabled(ctx)
                if (!frostEnabled) frost?.clear()
                it.transparency = transparency
                it.smokeEnabled = FirePrefs.getSmokeEnabled(ctx)
                it.smokeDensity = FirePrefs.getSmokeDensity(ctx)
                it.mode = when (FirePrefs.getMode(ctx)) {
                    1 -> FireSimulation.Mode.RAIN
                    2 -> FireSimulation.Mode.SNOW
                    else -> FireSimulation.Mode.FIRE
                }

            }
            rd?.let {
                it.enabled = FirePrefs.getRDEnabled(ctx)
                it.presetIndex = FirePrefs.getRDPreset(ctx)
                it.stepsPerFrame = FirePrefs.getRDSpeed(ctx).toInt()
            }
            licht?.let {
                it.enabled = FirePrefs.getLichtEnabled(ctx)
                it.intensity = FirePrefs.getLichtIntensity(ctx)
            }

            val currentUri = FirePrefs.getImageUri(ctx)
            if (currentUri != lastUri || bgBitmap == null) {
                lastUri = currentUri
                loadBackground(currentUri)
            }

            val newScale = FirePrefs.getScale(ctx)
            if (newScale != scale) {
                scale = newScale
                recreateSimFromHolder()
            }
            helecho?.let {
                it.enabled = FirePrefs.getHelechoEnabled(ctx)
                it.intensity = FirePrefs.getHelechoIntensity(ctx)
            }
        }

        private fun loadBackground(uriStr: String?) {
            bgBitmap = null
            if (uriStr == null) return
            try {
                val uri = Uri.parse(uriStr)
                val resolver = contentResolver

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

                var sampleSize = 1
                if (bounds.outWidth > 1080 || bounds.outHeight > 1920) {
                    sampleSize = max(
                        1,
                        max((bounds.outWidth / 2) / 1080, (bounds.outHeight / 2) / 1920)
                    )
                }
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                resolver.openInputStream(uri)?.use {
                    bgBitmap = BitmapFactory.decodeStream(it, null, decodeOptions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bgBitmap = null
            }
        }

        private fun recreateSimFromHolder() {
            val holder = surfaceHolder ?: return
            val w = holder.surfaceFrame.width()
            val h = holder.surfaceFrame.height()
            if (w <= 0 || h <= 0) return
            createBuffers(w, h)
        }

        private fun createBuffers(width: Int, height: Int) {
            val cols = max(1, width / scale)
            val rows = max(1, height / scale)
            sim = FireSimulation(cols, rows)
            fireBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            frost = FrostLayer(cols, rows)
            frostBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            pile = SnowPile(cols, rows)
            pileBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            smokeBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)

            licht = Lichtenberg(cols, rows).also {
                it.enabled = FirePrefs.getLichtEnabled(this@FireWallpaperService)
                it.intensity = FirePrefs.getLichtIntensity(this@FireWallpaperService)
            }
            lichtBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            helecho = FractalHelecho(cols, rows).also {
                it.enabled = FirePrefs.getHelechoEnabled(this@FireWallpaperService)
                it.intensity = FirePrefs.getHelechoIntensity(this@FireWallpaperService)
            }
            helechoBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            rd = DielectricGrowth(cols, rows).also {
                it.enabled = FirePrefs.getRDEnabled(this@FireWallpaperService)
                it.presetIndex = FirePrefs.getRDPreset(this@FireWallpaperService)
                it.stepsPerFrame = FirePrefs.getRDSpeed(this@FireWallpaperService).toInt()
            }
            rdBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            rdBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
        }



        private fun registerSensors() {
            if (sensorActive) return
            val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val sensor = gyro ?: accel ?: return

            motionListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val raw = if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                        event.values[0] / 3.14f
                    } else {
                        event.values[0] / 9.81f
                    }
                    wind = (wind * 0.7f + raw * 0.3f).coerceIn(-1f, 1f)
                    sim?.wind = wind
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(motionListener, sensor, SensorManager.SENSOR_DELAY_GAME)
            sensorActive = true
        }

        private fun unregisterSensors() {
            if (!sensorActive) return
            motionListener?.let { sensorManager.unregisterListener(it) }
            motionListener = null
            sensorActive = false
        }


        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            NotificationManager.instance = this
            applicationContext
                .getSharedPreferences("fire_prefs", Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(prefsListener)
        }

        override fun onDestroy() {
            NotificationManager.instance = null
            applicationContext
                .getSharedPreferences("fire_prefs", Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(prefsListener)
            unregisterSensors()
            super.onDestroy()
        }


        fun triggerImpact(amount: Float) {
            sim?.acceptImpact(amount)
        }

        override fun onVisibilityChanged(vis: Boolean) {
            visible = vis
            if (vis) loadPrefs()
            handler.removeCallbacks(tick)
            handler.post(tick)
            if (!vis) unregisterSensors()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(tick)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            scale = FirePrefs.getScale(this@FireWallpaperService)
            createBuffers(width, height)
            loadPrefs()
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.BLACK)

                // 1. Imagen de fondo
                bgBitmap?.let { bmp ->
                    val cw = canvas.width.toFloat()
                    val ch = canvas.height.toFloat()
                    val bw = bmp.width.toFloat()
                    val bh = bmp.height.toFloat()
                    val s = max(cw / bw, ch / bh)
                    val sw = bw * s * cropScale
                    val sh = bh * s * cropScale
                    val cx = cw / 2 + cropX * cw
                    val cy = ch / 2 + cropY * ch
                    val dst = RectF(cx - sw / 2, cy - sh / 2, cx + sw / 2, cy + sh / 2)
                    canvas.drawBitmap(bmp, null, dst, null)
                }

                val s = sim
                val fBmp = fireBmp
                if (s == null || fBmp == null) return

                val fullRect = Rect(0, 0, canvas.width, canvas.height)


                fBmp.setPixels(s.pixels(), 0, s.w, 0, 0, s.w, s.h)
                firePaint.alpha = (brightness * 255).toInt()
                val layer = canvas.saveLayerAlpha(
                    0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
                    (transparency * 255).toInt()
                )
                canvas.drawBitmap(fBmp, null, fullRect, firePaint)
                canvas.restoreToCount(layer)


                val sBmp = smokeBmp
                if (s.smokeEnabled && sBmp != null) {
                    sBmp.setPixels(s.smokePixels(), 0, s.w, 0, 0, s.w, s.h)
                    canvas.drawBitmap(sBmp, null, fullRect, smokePaint)
                }

                val fr = frost
                val frBmp = frostBmp
                if (frostEnabled && fr != null && frBmp != null) {
                    frBmp.setPixels(fr.pixels(), 0, s.w, 0, 0, s.w, s.h)
                    canvas.drawBitmap(frBmp, null, fullRect, frostPaint)

                }

                val pl = pile
                val plBmp = pileBmp
                if (pl != null && plBmp != null && pl.enabled) {
                    plBmp.setPixels(pl.pixels(), 0, s.w, 0, 0, s.w, s.h)
                    canvas.drawBitmap(plBmp, null, fullRect, pilePaint)

                }
                val hl = helecho
                val hlBmp = helechoBmp
                if (hl != null && hlBmp != null && hl.enabled) {
                    hlBmp.setPixels(hl.pixels(), 0, hl.w, 0, 0, hl.w, hl.h)
                    canvas.drawBitmap(hlBmp, null, fullRect, helechoPaint)
                }
                val r = rd
                val rBmp = rdBmp
                if (r != null && rBmp != null && r.enabled) {
                    rBmp.setPixels(r.pixels(), 0, r.w, 0, 0, r.w, r.h)
                    canvas.drawBitmap(rBmp, null, fullRect, rdPaint)
                }
                val li = licht
                val liBmp = lichtBmp
                if (li != null && liBmp != null && li.enabled) {
                    liBmp.setPixels(li.pixels(), 0, li.w, 0, 0, li.w, li.h)
                    canvas.drawBitmap(liBmp, null, fullRect, lichtPaint)
                }

            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }

        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (!touchEnabled) return
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                sim?.ignite((event.x / scale).toInt(), (event.y / scale).toInt())
            }
        }
    }
}