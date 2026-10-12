package com.example.hellfirewallpaper

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.abs
import kotlin.random.Random

class FireSimulation(val w: Int, val h: Int) {

    var hue: Int = 0
        set(value) {
            if (value != field) {
                field = value
                buildPalette()
            }
        }

    // Altura del fuego: 1.25 = valor original del HTML
    var decayFactor: Float = 1.25f

    // Viento: -1.00 a 1.00
    var wind: Float = 0f

    private val heat = ByteArray(w * h)
    private val px = IntArray(w * h)
    private val palette = IntArray(37)

    init {
        buildPalette()
        resetBase()
    }

    private fun buildPalette() {
        for (i in 0..36) {
            palette[i] = if (i == 36) {
                Color.WHITE
            } else {
                ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), 1f, i / 36f))
            }
        }
    }

    // Fila de abajo siempre encendida (la fuente del fuego)
    private fun resetBase() {
        for (x in 0 until w) {
            heat[(h - 1) * w + x] = 36
        }
    }

    fun step() {
        val windChance = abs(wind) * 0.5f
        val windDir = if (wind > 0f) 1 else -1

        for (y in 0 until h - 1) {
            for (x in 0 until w) {
                // Paso horizontal aleatorio: -1, 0 o +1 (como el original)
                var dx = Random.nextInt(3) - 1
                // El viento sesga algunos píxeles hacia su lado
                if (wind != 0f && Random.nextFloat() < windChance) {
                    dx = windDir
                }
                val tx = (x + dx).coerceIn(0, w - 1)
                val src = heat[(y + 1) * w + x].toInt() and 0xFF
                // Pérdida de calor: igual que el original, Math.random() * decay truncado
                val decay = (Random.nextFloat() * decayFactor).toInt()
                heat[y * w + tx] = (src - decay).coerceAtLeast(0).toByte()
            }
        }
        resetBase()
    }

    // Enciende una llamarada alrededor de una celda (para el toque)
    fun ignite(cx: Int, cy: Int, r: Int = 4) {
        for (dy in -r..r) {
            for (dx in -r..r) {
                if (dx * dx + dy * dy > r * r) continue
                val gx = cx + dx
                val gy = cy + dy
                if (gx in 0 until w && gy in 0 until h && Random.nextFloat() < 0.8f) {
                    heat[gy * w + gx] = 36
                }
            }
        }
    }

    fun pixels(): IntArray {
        for (i in heat.indices) {
            val hv = heat[i].toInt() and 0xFF
            px[i] = if (hv <= 5) 0 else palette[hv]
        }
        return px
    }
}