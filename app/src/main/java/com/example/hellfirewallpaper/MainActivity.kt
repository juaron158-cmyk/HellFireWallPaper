package com.example.hellfirewallpaper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var lang by remember { mutableStateOf(FirePrefs.getLang(context)) }
                    var hue by remember { mutableStateOf(FirePrefs.getHue(context).toFloat()) }
                    var decay by remember { mutableStateOf(FirePrefs.getDecay(context).toFloat()) }
                    var wind by remember { mutableStateOf(FirePrefs.getWind(context)) }
                    var fps by remember { mutableStateOf((1000f / FirePrefs.getTickMs(context)).coerceIn(5f, 60f)) }
                    var scale by remember { mutableStateOf(FirePrefs.getScale(context).toFloat()) }
                    var touchEnabled by remember { mutableStateOf(FirePrefs.getTouch(context)) }
                    var imageUri by remember { mutableStateOf(FirePrefs.getImageUri(context)) }

                    val picker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            context.contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            FirePrefs.saveImageUri(context, uri.toString())
                            FirePrefs.saveCrop(context, 1f, 0f, 0f)
                            imageUri = uri.toString()
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Selector de idioma
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(
                                "" to stringResource(R.string.lang_auto),
                                "es" to stringResource(R.string.lang_es),
                                "en" to stringResource(R.string.lang_en)
                            )
                            options.forEachIndexed { index, (code, label) ->
                                SegmentedButton(
                                    selected = lang == code,
                                    onClick = {
                                        lang = code
                                        FirePrefs.saveLang(context, code)
                                        (context as? Activity)?.recreate()
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) { Text(label) }
                            }
                        }
                        Spacer(modifier = Modifier.padding(top = 8.dp))

                        FirePreview(hue = hue, decay = decay, wind = wind, scale = scale.toInt())
                        Spacer(modifier = Modifier.padding(top = 8.dp))

                        Text(stringResource(R.string.fire_color, hue.toInt()))
                        Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            onValueChangeFinished = {
                                FirePrefs.save(context, hue.toInt(), decay.toInt())
                            },
                            valueRange = 0f..360f
                        )

                        Text(stringResource(R.string.fire_height, String.format("%.2f", decay / 100)))
                        Slider(
                            value = decay,
                            onValueChange = { decay = it },
                            onValueChangeFinished = {
                                FirePrefs.save(context, hue.toInt(), decay.toInt())
                            },
                            valueRange = 50f..200f
                        )

                        Text(stringResource(R.string.wind_label, String.format("%.2f", wind)))
                        Slider(
                            value = wind,
                            onValueChange = { wind = it },
                            onValueChangeFinished = { FirePrefs.saveWind(context, wind) },
                            valueRange = -1f..1f
                        )

                        Text(stringResource(R.string.speed_label, fps.toInt()))
                        Slider(
                            value = fps,
                            onValueChange = { fps = it },
                            onValueChangeFinished = {
                                FirePrefs.saveTickMs(context, (1000f / fps).toInt().coerceIn(16, 200))
                            },
                            valueRange = 5f..60f
                        )

                        Text(stringResource(R.string.granularity_label, scale.toInt()))
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            onValueChangeFinished = {
                                FirePrefs.saveScale(context, scale.toInt())
                            },
                            valueRange = 2f..16f,
                            steps = 14
                        )

                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            Text(stringResource(R.string.reactive_fire))
                            Switch(
                                checked = touchEnabled,
                                onCheckedChange = {
                                    touchEnabled = it
                                    FirePrefs.saveTouch(context, it)
                                }
                            )
                        }

                        Button(
                            onClick = { picker.launch(arrayOf("image/*")) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(if (imageUri == null) R.string.choose_bg else R.string.change_img))
                        }

                        val currentUri = imageUri
                        if (currentUri != null) {
                            Text(stringResource(R.string.img_selected), modifier = Modifier.padding(top = 8.dp))

                            Button(
                                onClick = {
                                    context.startActivity(Intent(context, CropActivity::class.java))
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(stringResource(R.string.crop_img))
                            }

                            Button(
                                onClick = {
                                    try {
                                        context.contentResolver.releasePersistableUriPermission(
                                            Uri.parse(currentUri),
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    } catch (e: Exception) {
                                    }
                                    FirePrefs.saveImageUri(context, null)
                                    FirePrefs.saveCrop(context, 1f, 0f, 0f)
                                    imageUri = null
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(stringResource(R.string.remove_img))
                            }
                        }
                    }
                }
            }
        }
    }
}