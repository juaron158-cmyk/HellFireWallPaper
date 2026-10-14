package com.example.hellfirewallpaper.ui

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionFuego(context: Context, onChanged: () -> Unit) {
    // Estado local: el chip seleccionado se actualiza al instante al pulsar
    var modoActual by remember { mutableIntStateOf(FirePrefs.getMode(context)) }

    Text(stringResource(R.string.mode_label))
    val modos = listOf(R.string.mode_fire, R.string.mode_rain, R.string.mode_snow)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modos.forEachIndexed { i, res ->
            FilterChip(
                selected = modoActual == i,
                onClick = {
                    modoActual = i
                    FirePrefs.saveMode(context, i)
                    onChanged()
                },
                label = { Text(stringResource(res)) }
            )
        }
    }
    when (modoActual) {
        1 -> { // Lluvia
            SliderSetting(
                label = { context.getString(R.string.rain_length_label, it.toInt()) },
                value = FirePrefs.getRainLength(context),
                range = 4f..30f,
                onSave = {
                    FirePrefs.saveRainLength(context, it)
                    onChanged()
                }
            )
            SliderSetting(
                label = { context.getString(R.string.fall_speed_label, String.format("%.2f", it)) },
                value = FirePrefs.getFallSpeed(context),
                range = 0f..2f,
                onSave = { FirePrefs.saveFallSpeed(context, it) }
            )
        }
        2 -> { // Nieve
            SliderSetting(
                label = { context.getString(R.string.snow_density_label, String.format("%.2f", it)) },
                value = FirePrefs.getSnowDensity(context),
                range = 0f..1f,
                onSave = {
                    FirePrefs.saveSnowDensity(context, it)
                    onChanged()
                }
            )
            SliderSetting(
                label = { context.getString(R.string.fall_speed_label, String.format("%.2f", it)) },
                value = FirePrefs.getFallSpeed(context),
                range = 0f..2f,
                onSave = { FirePrefs.saveFallSpeed(context, it) }
            )
        }
    }

    SliderSetting(
        label = { context.getString(R.string.fire_color, it.toInt()) },
        value = FirePrefs.getHue(context).toFloat(),
        range = 0f..360f,
        onSave = {
            FirePrefs.save(context, it.toInt(), FirePrefs.getDecay(context))
            onChanged()
        }
    )

    SliderSetting(
        label = { context.getString(R.string.fire_height, String.format("%.2f", it / 100f)) },
        value = FirePrefs.getDecay(context).toFloat(),
        range = 50f..200f,
        onSave = {
            FirePrefs.save(context, FirePrefs.getHue(context), it.toInt())
            onChanged()
        }
    )

    SwitchSetting(
        label = stringResource(R.string.smoke_label),
        checked = FirePrefs.getSmokeEnabled(context),
        onChange = {
            FirePrefs.saveSmokeEnabled(context, it)
            onChanged()
        }
    )
    if (FirePrefs.getSmokeEnabled(context)) {
        SliderSetting(
            label = { context.getString(R.string.smoke_density_label, String.format("%.2f", it)) },
            value = FirePrefs.getSmokeDensity(context),
            range = 0f..1f,
            onSave = {
                FirePrefs.saveSmokeDensity(context, it)
                onChanged()
            }
        )
    }

    SwitchSetting(
        label = stringResource(R.string.reactive_fire),
        checked = FirePrefs.getTouch(context),
        onChange = { FirePrefs.saveTouch(context, it) }
    )
}

@Composable
fun SeccionHielo(context: Context, onChanged: () -> Unit) {
    SwitchSetting(
        label = stringResource(R.string.frost_label),
        checked = FirePrefs.getFrostEnabled(context),
        onChange = {
            FirePrefs.saveFrostEnabled(context, it)
            onChanged()
        }
    )

    // Visible siempre, como antes
    SliderSetting(
        label = { context.getString(R.string.frost_size_label, (it * 100).toInt()) },
        value = FirePrefs.getFrostSize(context),
        range = 0.3f..2f,
        onSave = { FirePrefs.saveFrostSize(context, it) }
    )

    SwitchSetting(
        label = stringResource(R.string.helecho_label),
        checked = FirePrefs.getHelechoEnabled(context),
        onChange = {
            FirePrefs.saveHelechoEnabled(context, it)
            onChanged()
        }
    )
    if (FirePrefs.getHelechoEnabled(context)) {
        SliderSetting(
            label = { context.getString(R.string.helecho_intensity_label, (it * 100).toInt()) },
            value = FirePrefs.getHelechoIntensity(context),
            range = 0f..1f,
            onSave = { FirePrefs.saveHelechoIntensity(context, it) }
        )
    }
}

