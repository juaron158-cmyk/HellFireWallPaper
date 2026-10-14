package com.example.hellfirewallpaper

import android.graphics.Color

class MultiLichtenberg(val width: Int, val height: Int, val numRays: Int = 3) {

    private val rays = mutableListOf<Lichtenberg>()
    private val combinedPixels = IntArray(width * height)

    init {
        repeat(numRays) {
            val ray = Lichtenberg(width, height)
            ray.color = getRandomBlueColor()
            ray.intensity = 0.3f + kotlin.random.Random.nextFloat() * 0.4f
            ray.enabled = true
            rays.add(ray)
        }
    }

    fun step() {

        combinedPixels.fill(0)

        // Actualizar todos los rayos
        rays.forEach { ray ->
            ray.step()
        }

        // Combinar píxeles (suma de colores con alpha blending)
        for (i in combinedPixels.indices) {
            var finalAlpha = 0
            var finalR = 0
            var finalG = 0
            var finalB = 0

            rays.forEach { ray ->
                val pixel = ray.pixels()[i]
                if (pixel != 0) {
                    val a = android.graphics.Color.alpha(pixel)
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)

                    // Alpha blending simple (puedes mejorar con blend modes)
                    finalR = (finalR + r * a / 255).coerceIn(0, 255)
                    finalG = (finalG + g * a / 255).coerceIn(0, 255)
                    finalB = (finalB + b * a / 255).coerceIn(0, 255)
                    finalAlpha = ((finalAlpha + a) / 2).coerceIn(0, 255) // promedio simple
                }
            }

            combinedPixels[i] = if (finalAlpha > 0) {
                android.graphics.Color.argb(finalAlpha, finalR, finalG, finalB)
            } else {
                0
            }
        }
    }

    fun pixels(): IntArray = combinedPixels

    private fun getRandomBlueColor(): Int {
        val r = (100..200).random()
        val g = (150..230).random()
        val b = (230..255).random()
        return Color.rgb(r, g, b)
    }
}

