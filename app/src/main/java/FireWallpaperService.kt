package com.example.hellfirewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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
        private var scale = 8 // granulado: 2=pequeño/detallado, 8=medio, 16=grande/rápido

        private var sim: FireSimulation? = null
        private var fireBmp: Bitmap? = null
        private val paint = Paint().apply { isFilterBitmap = false }
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private var touchEnabled = true
        private var tickMs = 50L
        private var hue = 0
        private var decay = 1.25f
        private var wind = 0f

        private var cropScale = 1f
        private var cropX = 0f
        private var cropY = 0f

        private var bgBitmap: Bitmap? = null
        private var lastUri: String? = null

        private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            handler.post { loadPrefs() }
        }

        private val tick = object : Runnable {
            override fun run() {
                if (visible) {
                    sim?.step()
                    draw()
                    handler.postDelayed(this, tickMs)
                }
            }
        }

        init {
            loadPrefs()
        }

        private fun loadPrefs() {
            val ctx: Context = this@FireWallpaperService
            hue = FirePrefs.getHue(ctx)
            decay = FirePrefs.getDecay(ctx) / 100f
            wind = FirePrefs.getWind(ctx)
            touchEnabled = FirePrefs.getTouch(ctx)
            tickMs = FirePrefs.getTickMs(ctx).toLong()
            cropScale = FirePrefs.getCropScale(ctx)
            cropX = FirePrefs.getCropX(ctx)
            cropY = FirePrefs.getCropY(ctx)

            sim?.let {
                it.hue = hue
                it.decayFactor = decay
                it.wind = wind
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
        }

        private fun loadBackground(uriStr: String?) {
            bgBitmap = null
            if (uriStr == null) return

            try {
                val uri = Uri.parse(uriStr)
                val resolver = contentResolver

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                var sampleSize = 1
                if (options.outWidth > 1080 || options.outHeight > 1920) {
                    sampleSize = max(1, max((options.outWidth / 2) / 1080, (options.outHeight / 2) / 1920))
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
            val cols = max(1, w / scale)
            val rows = max(1, h / scale)
            sim = FireSimulation(cols, rows)
            fireBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            applicationContext
                .getSharedPreferences("fire_prefs", Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(prefsListener)
        }

        override fun onDestroy() {
            applicationContext
                .getSharedPreferences("fire_prefs", Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(prefsListener)
            super.onDestroy()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            scale = FirePrefs.getScale(this@FireWallpaperService)
            val cols = max(1, width / scale)
            val rows = max(1, height / scale)
            sim = FireSimulation(cols, rows)
            fireBmp = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
            loadPrefs()
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.BLACK)

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
                    val dstRect = RectF(cx - sw / 2, cy - sh / 2, cx + sw / 2, cy + sh / 2)
                    canvas.drawBitmap(bmp, null, dstRect, null)
                }

                val s = sim
                val fBmp = fireBmp
                if (s != null && fBmp != null) {
                    fBmp.setPixels(s.pixels(), 0, s.w, 0, 0, s.w, s.h)
                    canvas.drawBitmap(fBmp, null, Rect(0, 0, canvas.width, canvas.height), paint)
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

        override fun onVisibilityChanged(v: Boolean) {
            visible = v
            if (v) {
                loadPrefs()
                handler.removeCallbacks(tick)
                handler.post(tick)
            } else {
                handler.removeCallbacks(tick)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(tick)
        }
    }
}