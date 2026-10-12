package com.example.hellfirewallpaper

import android.graphics.Bitmap
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
fun FirePreview(hue: Float, decay: Float, wind: Float, scale: Int) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val ratio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()

    // Misma resolución que el wallpaper: ancho en píxeles reales / scale
    val screenPx = config.screenWidthDp * density.density
    val cols = (screenPx / scale).toInt().coerceIn(24, 540)
    val rows = (cols / ratio).toInt().coerceAtLeast(1)

    // Recrear simulación si cambia el scale
    val sim = remember(scale) { FireSimulation(cols, rows) }
    val bitmap = remember(scale) { Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888) }
    var frame by remember { mutableIntStateOf(0) }

    sim.hue = hue.toInt()
    sim.decayFactor = decay / 100f
    sim.wind = wind

    LaunchedEffect(scale) {
        while (true) {
            sim.step()
            bitmap.setPixels(sim.pixels(), 0, cols, 0, 0, cols, rows)
            frame++
            delay(50)
        }
    }

    val imageBitmap = remember(frame) { bitmap.asImageBitmap() }

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