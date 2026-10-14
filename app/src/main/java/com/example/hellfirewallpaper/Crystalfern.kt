package com.example.hellfirewallpaper

import android.graphics.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class FractalHelecho(val w: Int, val h: Int) {

    companion object {
        private const val MAX_GEN = 4          // niveles de ramificación (antes 4)
        private const val STEP = 1.2f          // avance por frame de cada punta
        private const val FADE = 0.98f         // estela más persistente
        private const val MAX_TIPS = 500       // más puntas simultáneas
        private const val SPAWN_MIN = 60        // frames mínimos entre brotes
        private const val SPAWN_RANGE = 8      // variación aleatoria de los brotes
        private const val BRANCH_BASE = 0.05f  // probabilidad base de bifurcar por paso
        private const val SPREAD = 0.45f       // ángulo de bifurcación (~26°)
    }

    private class Tip(
        var x: Float,
        var y: Float,
        var angle: Float,
        var len: Int,
        val gen: Int
    )

    private val glow = FloatArray(w * h)
    private val px = IntArray(w * h)
    private val tips = ArrayList<Tip>()
    private var spawnTimer = 0

    var color: Int = Color.rgb(200, 230, 255)

    var intensity: Float = 0.5f
        set(value) { field = value.coerceIn(0f, 1f) }

    var enabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) {
                    tips.clear()
                    glow.fill(0f)
                    px.fill(0)
                    spawnTimer = 0
                }
            }
        }

    fun step() {
        if (!enabled) return

        // Brotes desde los bordes: más cantidad cuanta más intensidad
        spawnTimer--
        if (spawnTimer <= 0) {
            val burst = 1 + (intensity * 2f).toInt()
            repeat(burst) {
                if (tips.size < MAX_TIPS) spawnRoot()
            }
            spawnTimer = SPAWN_MIN + Random.nextInt(SPAWN_RANGE)
        }

        // Avanzar las puntas que existían al inicio del frame
        val count = tips.size
        for (i in 0 until count) {
            advance(tips[i])
        }
        tips.removeAll { it.len <= 0 }

        // Desvanecer la estela y generar píxeles
        val tint = 0.5f + 0.5f * intensity
        for (i in glow.indices) {
            glow[i] *= FADE
            val a = glow[i]
            px[i] = if (a < 0.02f) 0 else Color.argb(
                (a * 255f * tint).toInt().coerceIn(0, 255),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }
    }

    fun pixels(): IntArray = px

    private fun spawnRoot() {
        val cx = w / 2f
        val cy = h / 2f
        val x: Float
        val y: Float
        when (Random.nextInt(4)) {
            0 -> { x = Random.nextFloat() * w; y = 0f }
            1 -> { x = Random.nextFloat() * w; y = (h - 1).toFloat() }
            2 -> { x = 0f; y = Random.nextFloat() * h }
            else -> { x = (w - 1).toFloat(); y = Random.nextFloat() * h }
        }

        // Dirigidas al centro con más variación, para que se vean más frondas
        val angle = atan2(cy - y, cx - x) + (Random.nextFloat() - 0.5f) * 0.9f
        val len = 40 + (intensity * 60f).toInt() + Random.nextInt(20)
        tips.add(Tip(x, y, angle, len, 0))
    }

    private fun advance(t: Tip) {
        t.x += cos(t.angle) * STEP
        t.y += sin(t.angle) * STEP
        t.len--

        if (t.x < 0f || t.x >= w || t.y < 0f || t.y >= h) {
            t.len = 0
            return
        }

        plot(t.x, t.y, t.gen)

        // Bifurcación: las ramas nuevas son más cortas y se separan del eje
        if (t.gen < MAX_GEN && t.len > 6 &&
            Random.nextFloat() < BRANCH_BASE * (0.5f + intensity * 1.5f)
        ) {
            val side = if (Random.nextBoolean()) 1f else -1f
            val spread = SPREAD * (0.7f + Random.nextFloat() * 0.6f)
            if (tips.size < MAX_TIPS) {
                tips.add(
                    Tip(
                        t.x, t.y,
                        t.angle + side * spread,
                        (t.len * 0.7f).toInt(),
                        t.gen + 1
                    )
                )
            }
        }

        // Pequeñas ramas laterales en las frondas principales (efecto de hojuela)
        if (t.gen <= 2 && t.len % 5 == 0 && t.len > 8 && tips.size < MAX_TIPS) {
            val side = if (t.len % 10 == 0) 1f else -1f
            tips.add(
                Tip(
                    t.x, t.y,
                    t.angle + side * (SPREAD * 1.3f),
                    6 + Random.nextInt(6),
                    t.gen + 2
                )
            )
        }
    }

    private fun plot(x: Float, y: Float, gen: Int) {
        val ix = x.toInt()
        val iy = y.toInt()
        if (ix !in 0 until w || iy !in 0 until h) return

        val b = (1f - gen * 0.09f).coerceAtLeast(0.35f)
        setMax(ix, iy, b)

        // Trazo de 2 píxeles en las ramas principales para que se vea más denso
        if (gen <= 2) {
            setMax(ix + 1, iy, b * 0.6f)
            setMax(ix, iy + 1, b * 0.6f)
        }
    }

    private fun setMax(x: Int, y: Int, value: Float) {
        if (x !in 0 until w || y !in 0 until h) return
        val i = y * w + x
        if (value > glow[i]) glow[i] = value
    }
}