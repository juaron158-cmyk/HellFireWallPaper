package com.example.hellfirewallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FirePreview(
    hue: Float,
    decay: Float,
    wind: Float,
    scale: Int,
    fireMode: Int = 0,              // 0=FUEGO, 1=LLUVIA, 2=NIEVE

    smokeEnabled: Boolean = false,
    smokeDensity: Float = 0.5f,
    brightness: Float = 1f,
    transparency: Float = 1f,
    fallSpeed: Float = 1f,
    rainLength: Float = 12f,
    snowDensity: Float = 0.5f,
    frostEnabled: Boolean = false,
    frostSize: Float = 1f
) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val ratio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()

    val screenPx = config.screenWidthDp * density.density
    val cols = (screenPx / scale).toInt().coerceIn(24, 540)
    val rows = (cols / ratio).toInt().coerceAtLeast(1)

    val sim = remember(cols, rows) { FireSimulation(cols, rows) }
    val frost = remember(cols, rows) { FrostLayer(cols, rows) }
    val out = remember(cols, rows) { Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888) }
    val fireBmp = remember(cols, rows) { Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888) }
    val smokeBmp = remember(cols, rows) { Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888) }
    val frostBmp = remember(cols, rows) { Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888) }
    val canvas = remember(out) { Canvas(out) }

    val firePaint = remember { Paint().apply { isFilterBitmap = false } }
    val smokePaint = remember { Paint().apply { isFilterBitmap = false } }
    val frostPaint = remember { Paint().apply { isFilterBitmap = false } }

    var frame by remember { mutableIntStateOf(0) }

    // Parámetros siempre al día (se leen en el bucle de animación)
    sim.hue = hue.toInt()
    sim.decayFactor = decay / 100f
    sim.wind = wind
    sim.brightness = brightness
    sim.transparency = transparency

    sim.smokeEnabled = smokeEnabled
    sim.smokeDensity = smokeDensity
    sim.fallSpeed = fallSpeed
    sim.rainLength = rainLength
    sim.snowDensity = snowDensity
    sim.mode = when (fireMode) {
        1 -> FireSimulation.Mode.RAIN
        2 -> FireSimulation.Mode.SNOW
        else -> FireSimulation.Mode.FIRE
    }
    frost.size = frostSize

    val frostOn by rememberUpdatedState(frostEnabled)

    LaunchedEffect(cols, rows) {
        var wasFrostOn = false
        while (true) {
            sim.step()
            if (frostOn) {
                frost.step(sim.impact)
                wasFrostOn = true
            } else if (wasFrostOn) {
                frost.clear()
                wasFrostOn = false
            }

            // Componer la vista previa: fuego, humo y copos
            out.eraseColor(Color.BLACK)


            fireBmp.setPixels(sim.pixels(), 0, cols, 0, 0, cols, rows)
            firePaint.alpha = (brightness * 255).toInt()
            canvas.drawBitmap(fireBmp, 0f, 0f, firePaint)

            if (sim.smokeEnabled) {
                smokeBmp.setPixels(sim.smokePixels(), 0, cols, 0, 0, cols, rows)
                canvas.drawBitmap(smokeBmp, 0f, 0f, smokePaint)
            }

            if (frostOn) {
                frostBmp.setPixels(frost.pixels(), 0, cols, 0, 0, cols, rows)
                canvas.drawBitmap(frostBmp, 0f, 0f, frostPaint)
            }

            frame++
            delay(50)
        }
    }


    val imageBitmap = remember(frame) { out.asImageBitmap() }

    Column {
        Text(stringResource(R.string.preview_title))
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .width(110.dp)
                .aspectRatio(ratio)
        )
    }
}