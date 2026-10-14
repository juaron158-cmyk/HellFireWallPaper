package com.example.hellfirewallpaper

import android.graphics.Color
import kotlin.random.Random

class Lichtenberg(val w: Int, val h: Int) {

    companion object {
        private const val RELAX_ITERS = 25
        private const val ETA = 1.5f
        private const val HOLD_FRAMES = 40
    }

    private val phi = FloatArray(w * h)
    private val tree = BooleanArray(w * h)
    private val glow = FloatArray(w * h)
    private val px = IntArray(w * h)
    private val cand = IntArray(w * h)
    private val candW = FloatArray(w * h)
    private var holdTimer = 0
    private var finished = false

    var color: Int = Color.rgb(170, 210, 255)


    var intensity: Float = 0.5f
        set(value) { field = value.coerceIn(0f, 1f) }

    var enabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                reset()
            }
        }

    fun pixels(): IntArray = px

    fun step() {
        if (!enabled) return

        if (finished) {
            holdTimer--
            if (holdTimer <= 0) reset()
            render()
            return
        }

        relax()
        repeat(4 + (intensity * 6f).toInt()) {
            if (!finished) growOnce()
        }
        render()
    }

    private fun reset() {
        tree.fill(false)
        glow.fill(0f)
        px.fill(0)
        finished = false
        holdTimer = 0


        val sx = w / 2 + Random.nextInt(-w / 6, w / 6 + 1)
        val sy = h / 2 + Random.nextInt(-h / 6, h / 6 + 1)
        tree[sy * w + sx] = true
        phi[sy * w + sx] = 0f


        for (y in 0 until h) for (x in 0 until w) {
            val i = y * w + x
            phi[i] = if (x == 0 || y == 0 || x == w - 1 || y == h - 1) 1f
            else if (tree[i]) 0f else 0.5f
        }
    }


    private fun relax() {
        repeat(RELAX_ITERS) {
            for (y in 1 until h - 1) {
                for (x in 1 until w - 1) {
                    val i = y * w + x
                    if (tree[i]) continue
                    phi[i] = 0.25f * (phi[i - 1] + phi[i + 1] + phi[i - w] + phi[i + w])
                }
            }
        }
    }

    private fun growOnce() {
        var n = 0
        var total = 0f


        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                if (tree[i] || !touchesTree(x, y)) continue
                val wgt = Math.pow(phi[i].toDouble(), ETA.toDouble()).toFloat()
                cand[n] = i
                candW[n] = wgt
                total += wgt
                n++
            }
        }
        if (n == 0 || total <= 0f) {
            finished = true
            holdTimer = HOLD_FRAMES
            return
        }


        val r = Random.nextFloat() * total
        var acc = 0f
        var pick = cand[n - 1]
        for (k in 0 until n) {
            acc += candW[k]
            if (acc >= r) {
                pick = cand[k]
                break
            }
        }

        tree[pick] = true
        phi[pick] = 0f
        glow[pick] = 1f

        val x = pick % w
        val y = pick / w
        if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
            finished = true
            holdTimer = HOLD_FRAMES
        }
    }

    private fun touchesTree(x: Int, y: Int): Boolean {
        if (x > 0 && tree[y * w + x - 1]) return true
        if (x < w - 1 && tree[y * w + x + 1]) return true
        if (y > 0 && tree[(y - 1) * w + x]) return true
        if (y < h - 1 && tree[(y + 1) * w + x]) return true
        return false
    }

    private fun render() {

        val base = if (finished) 0.6f * holdTimer / HOLD_FRAMES else 0.35f
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        for (i in px.indices) {
            glow[i] *= 0.9f
            val v = if (tree[i]) maxOf(glow[i], base) else glow[i]
            px[i] = if (v < 0.02f) 0 else Color.argb((v * 255f).toInt().coerceIn(0, 255), r, g, b)
        }
    }
}