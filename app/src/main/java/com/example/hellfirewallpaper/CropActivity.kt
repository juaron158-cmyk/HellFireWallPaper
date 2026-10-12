package com.example.hellfirewallpaper

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

class CropActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Localization.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFF0000),
                    background = Color.Black,
                    surface = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                val context = LocalContext.current
                val config = LocalConfiguration.current
                val ratio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()
                val uriStr = FirePrefs.getImageUri(context)

                val imageBitmap = remember(uriStr) {
                    uriStr?.let { s ->
                        try {
                            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                            context.contentResolver.openInputStream(Uri.parse(s))?.use {
                                BitmapFactory.decodeStream(it, null, opts)
                            }?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                var previewSize by remember { mutableStateOf(IntSize.Zero) }
                var scale by remember { mutableStateOf(FirePrefs.getCropScale(context)) }
                var offX by remember { mutableStateOf(FirePrefs.getCropX(context)) }
                var offY by remember { mutableStateOf(FirePrefs.getCropY(context)) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (imageBitmap == null) {
                            Text(stringResource(R.string.no_image))
                        } else {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .aspectRatio(ratio)
                                    .clipToBounds()
                                    .onSizeChanged { previewSize = it }
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val w = previewSize.width.toFloat()
                                            val h = previewSize.height.toFloat()
                                            if (w == 0f || h == 0f) return@detectTransformGestures

                                            val newScale = (scale * zoom).coerceIn(0.2f, 5f)
                                            val maxX = max(0f, (newScale - 1f) * w / 2f)
                                            val maxY = max(0f, (newScale - 1f) * h / 2f)
                                            val newX = (offX * w + pan.x).coerceIn(-maxX, maxX)
                                            val newY = (offY * h + pan.y).coerceIn(-maxY, maxY)

                                            scale = newScale
                                            offX = newX / w
                                            offY = newY / h
                                        }
                                    }
                            ) {
                                val cw = size.width
                                val ch = size.height
                                val bw = imageBitmap.width.toFloat()
                                val bh = imageBitmap.height.toFloat()
                                val base = max(cw / bw, ch / bh)
                                val sw = bw * base * scale
                                val sh = bh * base * scale
                                val cx = cw / 2f + offX * cw
                                val cy = ch / 2f + offY * ch

                                drawImage(
                                    image = imageBitmap,
                                    dstOffset = IntOffset(
                                        (cx - sw / 2f).roundToInt(),
                                        (cy - sh / 2f).roundToInt()
                                    ),
                                    dstSize = IntSize(sw.roundToInt(), sh.roundToInt())
                                )
                            }
                        }

                        Text(
                            stringResource(R.string.zoom_label, String.format("%.2f", scale)),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Button(
                            onClick = {
                                FirePrefs.saveCrop(context, scale, offX, offY)
                                this@CropActivity.finish()
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(R.string.save_crop))
                        }
                    }
                }
            }
        }
    }
}