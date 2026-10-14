package com.example.hellfirewallpaper

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.abs
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class FireSimulation(val w: Int, val h: Int) {

    enum class Mode { FIRE, RAIN, SNOW }

    var hue: Int = 0
        set(value) {
            if (value != field) {
                field = value
                buildPalette()
            }
        }

    var decayFactor: Float = 1.25f
    var wind: Float = 0f
    var brightness: Float = 1f
        set(value) { field = value.coerceIn(0f, 1f) }
    var transparency: Float = 1f
        set(value) { field = value.coerceIn(0f, 1f) }
    var impact: Float = 0f

    var smokeEnabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) smoke.fill(0)
            }
        }
    var smokeDensity: Float = 0.5f
        set(value) { field = value.coerceIn(0f, 1f) }

    var mode: Mode = Mode.FIRE
        set(value) {
            if (value != field) {
                field = value
                resetBase()
            }
        }
    var rainLength: Float = 12f      // largo aproximado de la estela de lluvia (en filas)
        set(value) { field = value.coerceIn(4f, 30f) }

    var snowDensity: Float = 0.5f    // 0 = sin nieve, 1 = el doble de densidad
        set(value) { field = value.coerceIn(0f, 1f) }
    private val heat = ByteArray(w * h)
    private val smoke = IntArray(w * h)
    private val err = FloatArray(w * h)
    private val px = IntArray(w * h)
    private val smokePx = IntArray(w * h)
    private val palette = IntArray(37)

    init {
        buildPalette()
        resetBase()
    }

    private fun buildPalette() {
        for (i in 0..36) {
            palette[i] = if (i == 36) Color.WHITE
            else ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), 1f, i / 36f))
        }
    }

    private fun rainPalette(): IntArray = IntArray(37).apply {
        for (i in indices) this[i] =
            if (i == 36) Color.WHITE
            else ColorUtils.HSLToColor(floatArrayOf(210f, 0.7f, i / 36f))
    }

    private fun snowPalette(): IntArray = IntArray(37).apply {
        for (i in indices) this[i] =
            if (i == 36) Color.WHITE
            else Color.argb(((i / 36f) * 255).toInt().coerceAtLeast(40), 235, 240, 255)
    }

    private fun resetBase() {
        heat.fill(0)
        err.fill(0f)
        smoke.fill(0)
        when (mode) {
            Mode.FIRE -> for (x in 0 until w) heat[(h - 1) * w + x] = 36
            Mode.RAIN, Mode.SNOW -> {}
        }
    }

    fun acceptImpact(amount: Float) {
        impact = (impact + amount).coerceAtMost(1f)
    }

    fun ignite(cx: Int, cy: Int, r: Int = 4) {
        for (dy in -r..r) for (dx in -r..r) {
            if (dx * dx + dy * dy > r * r) continue
            val gx = cx + dx; val gy = cy + dy
            if (gx in 0 until w && gy in 0 until h && Random.nextFloat() < 0.8f) {
                heat[gy * w + gx] = 36
            }
        }
    }

    fun step() {
        impact *= 0.95f
        if (impact < 0.01f) impact = 0f

        when (mode) {
            Mode.FIRE -> {
                stepFire()
                stepSmoke()
            }
            Mode.RAIN -> {
                smoke.fill(0)
                stepRain()
            }
            Mode.SNOW -> {
                smoke.fill(0)
                stepSnow()
            }
        }
    }

    private fun stepFire() {
        val windChance = abs(wind) * 0.5f
        val windDir = if (wind > 0f) 1 else -1
        val extraHeat = if (impact > 0.01f) (impact * 5).toInt() else 0
        for (y in 0 until h - 1) {
            for (x in 0 until w) {
                var dx = Random.nextInt(3) - 1
                if (wind != 0f && Random.nextFloat() < windChance) dx = windDir
                val tx = (x + dx).coerceIn(0, w - 1)
                val src = heat[(y + 1) * w + x].toInt() and 0xFF
                val loss = (Random.nextFloat() * decayFactor).toInt()
                heat[y * w + tx] = (src - loss + extraHeat).coerceIn(0, 36).toByte()
            }
        }
        for (x in 0 until w) heat[(h - 1) * w + x] = 36
    }

    var fallSpeed: Float = 1.0f
        set(value) { field = value.coerceIn(0f, 2f) }

    private var fallAcc = 0f

    private fun windDrift(): Int = when {
        wind > 0.05f -> 1
        wind < -0.05f -> -1
        else -> 0
    }

    // Cuántas filas bajar en este frame. fallSpeed 0 = no cae nada.
    private fun fallShifts(factor: Float): Int {
        fallAcc += fallSpeed * factor
        var n = 0
        while (fallAcc >= 1f) {
            fallAcc -= 1f
            n++
        }
        return n
    }

    // Desplaza todo una fila hacia abajo y genera partículas nuevas arriba.
// Las filas de abajo salen de la pantalla; no hay índices negativos.
    private fun shiftDown(drift: Int, jitter: Float, spawnRate: Float) {
        for (y in h - 1 downTo 1) {
            val row = y * w
            val above = (y - 1) * w
            for (x in 0 until w) {
                var sx = x - drift
                if (jitter > 0f && Random.nextFloat() < jitter) {
                    sx += Random.nextInt(3) - 1   // deriva aleatoria leve (copos)
                }
                sx = sx.coerceIn(0, w - 1)
                heat[row + x] = heat[above + sx]
            }
        }
        for (x in 0 until w) {
            heat[x] = if (Random.nextFloat() < spawnRate) (20 + Random.nextInt(17)).toByte() else 0
        }
    }

    private fun stepRain() {
        val n = fallShifts(1f)
        val drift = windDrift()
        val spawn = 0.10f + impact * 0.4f
        val trail = (36f / rainLength).toInt().coerceAtLeast(1)
        repeat(n) { shiftRainStreaks(drift, trail, spawn) }
    }

    private fun shiftRainStreaks(drift: Int, trail: Int, spawnRate: Float) {
        // La cabeza de la gota baja una fila; el resto de la estela se apaga poco a poco
        for (y in h - 1 downTo 1) {
            val row = y * w
            val above = (y - 1) * w
            for (x in 0 until w) {
                val sx = (x - drift).coerceIn(0, w - 1)
                val head = heat[above + sx].toInt() and 0xFF
                val tail = (heat[row + x].toInt() and 0xFF) - trail
                heat[row + x] = maxOf(head, tail, 0).toByte()
            }
        }
        for (x in 0 until w) {
            val tail = (heat[x].toInt() and 0xFF) - trail
            heat[x] = (if (Random.nextFloat() < spawnRate) 36 else maxOf(tail, 0)).toByte()
        }
    }

    private fun stepSnow() {
        val n = fallShifts(0.5f)   // la nieve va a mitad de velocidad
        val drift = windDrift()
        val spawn = (0.06f + impact * 0.3f) * (snowDensity * 2f)
        repeat(n) { shiftDown(drift, 0.15f, spawn) }
    }

    private fun stepSmoke() {
        if (!smokeEnabled) {
            smoke.fill(0)
            return
        }
        val rate = smokeDensity * 0.25f
        val drift = when {
            wind > 0.05f -> 1
            wind < -0.05f -> -1
            else -> 0
        }

        // 1. Nacer en las celdas justo encima de una llama
        for (y in 1 until h - 1) {
            for (x in 0 until w) {
                val idx = y * w + x
                val here = heat[idx].toInt() and 0xFF
                val below = heat[idx + w].toInt() and 0xFF
                if (here <= 5 && below > 5 && Random.nextFloat() < rate) {
                    smoke[idx] = 80
                }
            }
        }

        // 2. Subir: de arriba hacia abajo, para que cada celda avance sólo una fila
        for (y in 1 until h) {
            val row = y * w
            val above = (y - 1) * w
            for (x in 0 until w) {
                val sx = (x - drift).coerceIn(0, w - 1)
                val src = smoke[row + sx]
                smoke[above + x] = if (src > 1) src - 1 - Random.nextInt(2) else 0
            }
        }
    }
    fun pixels(): IntArray {
        val pal = when (mode) {
            Mode.FIRE -> palette
            Mode.RAIN -> rainPalette()
            Mode.SNOW -> snowPalette()
        }
        for (i in heat.indices) {
            val hv = heat[i].toInt() and 0xFF
            px[i] = if (hv <= 5) 0 else pal[hv]
        }
        return px
    }
    // Calor de la fila inferior: indica si un copo acaba de llegar al fondo
    fun bottomHeat(x: Int): Int = heat[(h - 1) * w + x].toInt() and 0xFF
    // Lee o borra una celda de calor/nieve (la usa SnowPile para recoger copos)
    fun heatAt(x: Int, y: Int): Int = heat[y * w + x].toInt() and 0xFF

    fun clearCell(x: Int, y: Int) {
        heat[y * w + x] = 0
    }
    fun smokePixels(): IntArray {
        if (!smokeEnabled) { smokePx.fill(0); return smokePx }
        for (i in smoke.indices) {
            val s = smoke[i]
            smokePx[i] = if (s < 12) 0 else Color.argb(
                ((s - 12) * 1.5f).toInt().coerceAtMost(130),
                150, 150, 150
            )
        }
        return smokePx
    }
}