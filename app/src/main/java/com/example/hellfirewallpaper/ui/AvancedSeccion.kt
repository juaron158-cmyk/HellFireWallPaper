package com.example.hellfirewallpaper.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R

@Composable
fun SeccionAvanzado(context: Context, onChanged: () -> Unit) {
    SliderSetting(
        label = { context.getString(R.string.granularity_label, it.toInt()) },
        value = FirePrefs.getScale(context).toFloat(),
        range = 2f..16f,
        steps = 14,
        onSave = {
            FirePrefs.saveScale(context, it.toInt())
            onChanged()
        }
    )

    SliderSetting(
        label = { context.getString(R.string.speed_label, it.toInt()) },
        value = (1000f / FirePrefs.getTickMs(context)).coerceIn(5f, 60f),
        range = 5f..60f,
        onSave = {
            FirePrefs.saveTickMs(context, (1000f / it).toInt().coerceIn(16, 200))
        }
    )

    SliderSetting(
        label = { context.getString(R.string.brightness_label, (it * 100).toInt()) },
        value = FirePrefs.getBrightness(context) / 255f,
        range = 0.2f..1f,
        onSave = {
            FirePrefs.saveBrightness(context, (it * 255).toInt())
            onChanged()
        }
    )

    SliderSetting(
        label = { context.getString(R.string.transparency_label, (it * 100).toInt()) },
        value = FirePrefs.getTransparency(context) / 255f,
        range = 0.2f..1f,
        onSave = {
            FirePrefs.saveTransparency(context, (it * 255).toInt())
            onChanged()
        }
    )

    val gyro = FirePrefs.getGyro(context)
    SwitchSetting(
        label = stringResource(R.string.gyro_wind),
        checked = gyro,
        onChange = {
            FirePrefs.saveGyro(context, it)
            if (it) FirePrefs.saveWind(context, 0f)
            onChanged()
        }
    )

    if (!gyro) {
        SliderSetting(
            label = { context.getString(R.string.wind_label, String.format("%.2f", it)) },
            value = FirePrefs.getWind(context),
            range = -1f..1f,
            onSave = {
                FirePrefs.saveWind(context, it)
                onChanged()
            }
        )
    }
}
