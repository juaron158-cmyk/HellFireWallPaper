package com.example.hellfirewallpaper

import android.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// Varios copos de 6 brazos a la vez. Cada uno crece desde un punto fijo,
// se congela, se desvanece y puede solaparse con los demás.
class FrostLayer(val w: Int, val h: Int) {

    companion object {
        private const val LIFE = 240        // frames de vida por celda (~12 s a 50 ms)
        private const val ATTEMPTS = 80     // intentos de crecimiento por frame
        private const val BASE_GROW = 350   // frames de crecimiento con size = 1.0
        private const val MAX_CRYSTALS = 8  // copos simultáneos
        private const val SIDES = 6         // fijo: copo hexagonal
        private val DX4 = intArrayOf(-1, 1, 0, 0)
        private val DY4 = intArrayOf(0, 0, -1, 1)
    }

    var seedRate: Float = 0.03f         // probabilidad por frame de nuevo copo
    var snowflake: Boolean = true       // false = costra irregular sin simetría
    var size: Float = 1f                // tamaño del copo: 0.3 pequeño, 2.0 grande
        set(value) { field = value.coerceIn(0.3f, 2f) }
    var color: Int = Color.argb(255, 200, 230, 255)

    private val born = IntArray(w * h)
    private val owner = IntArray(w * h) { -1 }
    private val px = IntArray(w * h)
    private val inFront = BooleanArray(w * h)
    private val front = IntArray(w * h)
    private var frontCount = 0

    private val cX = IntArray(MAX_CRYSTALS)
    private val cY = IntArray(MAX_CRYSTALS)
    private val cBirth = IntArray(MAX_CRYSTALS)
    private val cActive = BooleanArray(MAX_CRYSTALS)

    private var frame = 0

    private fun maxGrow(): Int = (BASE_GROW * size).roundToInt()

    fun clear() {
        born.fill(0)
        owner.fill(-1)
        px.fill(0)
        inFront.fill(false)
        frontCount = 0
        cActive.fill(false)
        frame = 0
    }

    fun step(extraSeeds: Float = 0f) {
        frame++

        if (Random.nextFloat() < seedRate + extraSeeds) {
            val slot = freeSlot()
            if (slot >= 0) startCrystal(slot, Random.nextInt(w), Random.nextInt(h))
        }

        val limit = maxGrow()
        for (k in 0 until MAX_CRYSTALS) {
            if (cActive[k] && frame - cBirth[k] > limit) deactivate(k)
        }

        grow()

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        for (i in born.indices) {
            val b0 = born[i]
            if (b0 == 0) {
                px[i] = 0
                continue
            }
            val age = frame - b0
            if (age >= LIFE) {
                born[i] = 0
                owner[i] = -1
                px[i] = 0
            } else {
                val a = ((LIFE - age) * 255 / LIFE).coerceIn(0, 255)
                px[i] = Color.argb(a, r, g, b)
            }
        }
    }

    fun pixels(): IntArray = px

    private fun freeSlot(): Int {
        for (k in 0 until MAX_CRYSTALS) if (!cActive[k]) return k
        return -1
    }

    private fun startCrystal(k: Int, x: Int, y: Int) {
        cActive[k] = true
        cX[k] = x
        cY[k] = y
        cBirth[k] = frame
        place(x, y, k)
        addFront(x, y, k)
    }

    private fun deactivate(k: Int) {
        cActive[k] = false
        var i = frontCount - 1
        while (i >= 0) {
            if (owner[front[i]] == k) removeFront(i)
            i--
        }
    }

    // Marca la celda y sus 6 copias rotadas 60° alrededor del centro del copo k
    private fun place(x: Int, y: Int, k: Int) {
        setCell(x, y, k)
        if (!snowflake) return
        val dx = (x - cX[k]).toDouble()
        val dy = (y - cY[k]).toDouble()
        for (n in 1 until SIDES) {
            val angle = n * 2.0 * PI / SIDES
            val rx = (dx * cos(angle) - dy * sin(angle)).roundToInt() + cX[k]
            val ry = (dx * sin(angle) + dy * cos(angle)).roundToInt() + cY[k]
            if (rx in 0 until w && ry in 0 until h) setCell(rx, ry, k)
        }
    }

    private fun setCell(x: Int, y: Int, k: Int) {
        val i = y * w + x
        if (born[i] == 0) {
            born[i] = frame
            owner[i] = k
        }
    }

    private fun addFront(x: Int, y: Int, k: Int) {
        val i = y * w + x
        if (inFront[i] || frontCount >= front.size) return
        inFront[i] = true
        front[frontCount++] = i
    }

    private fun removeFront(idx: Int) {
        inFront[front[idx]] = false
        frontCount--
        front[idx] = front[frontCount]
    }

    private fun grow() {
        repeat(ATTEMPTS) {
            if (frontCount == 0) return
            val idx = Random.nextInt(frontCount)
            val cell = front[idx]
            val k = owner[cell]

            if (k < 0 || !cActive[k] || born[cell] == 0) {
                removeFront(idx)
                return@repeat
            }

            val x = cell % w
            val y = cell / w
            val d = Random.nextInt(4)
            val nx = x + DX4[d]
            val ny = y + DY4[d]
            if (nx !in 0 until w || ny !in 0 until h) return@repeat
            if (born[ny * w + nx] != 0) return@repeat
            if (Random.nextFloat() > 0.35f) return@repeat

            place(nx, ny, k)
            addFront(nx, ny, k)
        }
    }
}