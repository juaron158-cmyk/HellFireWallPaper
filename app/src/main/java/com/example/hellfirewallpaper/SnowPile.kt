package com.example.hellfirewallpaper


import android.graphics.Color
import kotlin.random.Random


// Montón de nieve por capas horizontales con física de arena.
// Cuando una columna se desborda, una capa se desliza lateralmente a una
// columna vecina vacía, creando pendientes naturales en lugar de "tenedores".
// La limpieza se acelera cuando hay presión para evitar acumulación descontrolada.
class SnowPile(val w: Int, val h: Int) {


    companion object {
        private const val DISSOLVE = 32           // pérdida de opacidad por frame en la capa del fondo
        private const val DISSOLVE_BOOST = 4     // extra por cada punto de presión (0..1)
        private const val MAX_FRACTION = 0.35f   // altura máxima de la pila (fracción de la pantalla)
        private const val DEPOSIT_CHANCE = 0.9f  // probabilidad de que un copo se quede
        private const val CLEAN_START = 0.45f    // a partir de este % de columnas llenas ya se limpia
    }


    var enabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) clear()
            }
        }


    private val alpha = IntArray(w * h)   // 0 = vacío; 1..255 = opacidad
    private val height = IntArray(w)      // capas apiladas por columna
    private val px = IntArray(w * h)
    private val maxH = (h * MAX_FRACTION).toInt().coerceAtLeast(2)
    private val cleanFrom = (maxH * CLEAN_START).toInt().coerceAtLeast(1)


    fun clear() {
        alpha.fill(0)
        height.fill(0)
        px.fill(0)
    }


    fun pixels(): IntArray = px


    fun step(sim: FireSimulation) {
        if (!enabled) return


        // 1. Recoger copos: si hay nieve justo encima de la pila (o en el fondo), se queda
        for (x in 0 until w) {
            if (height[x] >= maxH) continue
            val r = h - 1 - height[x]
            if (r >= 0 && sim.heatAt(x, r) > 0 && Random.nextFloat() < DEPOSIT_CHANCE) {
                sim.clearCell(x, r)
                height[x]++
                alpha[(h - height[x]) * w + x] = 255
            }
        }


        // 2. Física de arena: 80 iteraciones para distribuir la nieve
        for (iter in 0 until 80) {
            var moved = false
            for (x in 0 until w) {
                if (height[x] > maxH - 80) {
                    val side = if (Random.nextBoolean()) 1 else -1
                    val nx = x + side
                    if (nx >= 0 && nx < w && height[nx] < height[x] - 1) {
                        height[nx]++
                        alpha[(h - height[nx]) * w + nx] = 255
                        height[x]--
                        moved = true
                    }
                }
            }
            if (!moved) break
        }


        // 3. Presión: fracción de columnas casi llenas -> a más nieve, más rápido se limpia
        var full = 0
        for (x in 0 until w) if (height[x] >= cleanFrom) full++
        val pressure = if (w > 0) full.toFloat() / w else 0f
        val dissolve = (DISSOLVE + DISSOLVE_BOOST * pressure).toInt().coerceIn(DISSOLVE, 255)


        // Se limpia desde que la pila está "casi" llena, no hace falta llegar al tope
        for (x in 0 until w) {
            if (height[x] < cleanFrom) continue
            val i = (h - 1) * w + x
            alpha[i] = (alpha[i] - dissolve).coerceAtLeast(0)
            if (alpha[i] == 0) removeBottom(x)
        }


        // 4. Salida de píxeles
        for (i in alpha.indices) {
            val a = alpha[i]
            px[i] = if (a == 0) 0 else Color.argb(a, 235, 242, 255)
        }
    }


    // Quita la capa del fondo de la columna y sube el resto una fila
    private fun removeBottom(x: Int) {
        for (r in h - 1 downTo 1) {
            alpha[r * w + x] = alpha[(r - 1) * w + x]
        }
        alpha[x] = 0
        height[x]--
    }
}