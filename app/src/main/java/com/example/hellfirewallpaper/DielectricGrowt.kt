package com.example.hellfirewallpaper

import android.graphics.Color
import kotlin.math.pow
import kotlin.random.Random

class DielectricGrowth(val w: Int, val h: Int) {

    companion object {
        private const val RELAX_ITERS = 25    // iteraciones del campo por frame
        private const val HOLD_FRAMES = 60    // frames que la figura se queda antes de reiniciar
        private const val MAX_COVER = 0.30f   // al cubrir esta fracción, la figura se desvanece
        // Un preset por chip: [η (forma de las ramas), semillas]
        private val ETA = floatArrayOf(1.0f, 2.0f, 0.6f, 1.5f, 0.8f)
        private val SEEDS = intArrayOf(3, 8, 5, 1, 12)
    }

    private val phi = FloatArray(w * h)
    private val tree = BooleanArray(w * h)
    private val glow = FloatArray(w * h)
    private val px = IntArray(w * h)
    private val cand = IntArray(w * h)
    private val candW = FloatArray(w * h)
    private var treeCount = 0
    private var holdTimer = 0
    private var finished = false

    var color: Int = Color.rgb(170, 210, 255)

    var presetIndex: Int = 0
        set(value) {
            field = value.coerceIn(0, ETA.size - 1)
            if (enabled) reset()
        }


    var stepsPerFrame: Int = 2
        set(value) { field = value.coerceIn(1, 4) }

    var enabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (value) reset() else px.fill(0)
            }
        }

    init {
        reset()
    }

    fun pixels(): IntArray = px

    fun reset() {
        tree.fill(false)
        glow.fill(0f)
        px.fill(0)
        treeCount = 0
        finished = false
        holdTimer = 0


        for (y in 0 until h) for (x in 0 until w) {
            val edge = x == 0 || y == 0 || x == w - 1 || y == h - 1
            phi[y * w + x] = if (edge) 1f else 0.5f
        }


        repeat(SEEDS[presetIndex]) {
            val sx = w / 2 + Random.nextInt(-w / 4, w / 4 + 1)
            val sy = h / 2 + Random.nextInt(-h / 4, h / 4 + 1)
            addCell(sx, sy)
        }
    }

    fun step() {
        if (!enabled) return

        if (finished) {
            holdTimer--
            if (holdTimer <= 0) reset()
        } else {
            relax()
            val n = buildCandidates()
            if (n == 0) {
                finished = true
                holdTimer = HOLD_FRAMES
            } else {
                repeat(stepsPerFrame) { pickOne(n) }
                if (treeCount > (w * h * MAX_COVER).toInt()) {
                    finished = true
                    holdTimer = HOLD_FRAMES
                }
            }
        }
        render()
    }


    private fun relax() {
        repeat(RELAX_ITERS) {
            for (y in 1 until h - 1) for (x in 1 until w - 1) {
                val i = y * w + x
                if (tree[i]) continue
                phi[i] = 0.25f * (phi[i - 1] + phi[i + 1] + phi[i - w] + phi[i + w])
            }
        }
    }


    private fun buildCandidates(): Int {
        var n = 0
        val eta = ETA[presetIndex].toDouble()
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val i = y * w + x
            if (tree[i] || !touchesTree(x, y)) continue
            val wgt = phi[i].toDouble().pow(eta).toFloat()
            cand[n] = i
            candW[n] = wgt
            n++
        }
        return n
    }

    private fun pickOne(n: Int) {
        var total = 0f
        for (k in 0 until n) total += candW[k]
        if (total <= 0f) return

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
        addCell(pick % w, pick / w)
    }

    private fun addCell(x: Int, y: Int) {
        if (x !in 1 until w - 1 || y !in 1 until h - 1) return
        val i = y * w + x
        if (tree[i]) return
        tree[i] = true
        phi[i] = 0f
        glow[i] = 1f
        treeCount++
    }

    private fun touchesTree(x: Int, y: Int): Boolean =
        tree[y * w + x - 1] || tree[y * w + x + 1] ||
                tree[(y - 1) * w + x] || tree[(y + 1) * w + x]

    private fun render() {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val fade = if (finished) holdTimer.toFloat() / HOLD_FRAMES else 1f
        for (i in px.indices) {
            glow[i] *= 0.9f
            var v = if (tree[i]) maxOf(glow[i], 0.45f) else glow[i]
            v *= fade
            px[i] = if (v < 0.02f) 0
            else Color.argb((v * 255f).toInt().coerceIn(0, 255), r, g, b)
        }
    }
}