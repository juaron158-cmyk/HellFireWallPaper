package com.example.hellfirewallpaper.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R
import androidx.compose.material3.Switch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionPatrones(onChanged: () -> Unit) {
    val context = LocalContext.current
    val nombres = listOf(
        stringResource(R.string.rd_manchas),
        stringResource(R.string.rd_coral),
        stringResource(R.string.rd_laberinto),
        stringResource(R.string.rd_mitosis),
        stringResource(R.string.rd_gusanos)
    )

    var enabled by remember { mutableStateOf(FirePrefs.getRDEnabled(context)) }
    var preset by remember { mutableIntStateOf(FirePrefs.getRDPreset(context)) }
    var speed by remember { mutableFloatStateOf(FirePrefs.getRDSpeed(context)) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.rd_label), modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                FirePrefs.saveRDEnabled(context, it)
                onChanged()
            }
        )
    }

    if (enabled) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nombres.forEachIndexed { index, nombre ->
                FilterChip(
                    selected = preset == index,
                    onClick = {
                        preset = index
                        FirePrefs.saveRDPreset(context, index)
                    },
                    label = { Text(nombre) }
                )
            }
        }

        Column {
            Text(stringResource(R.string.rd_speed_label, String.format("%.0f", speed)))
            Slider(
                value = speed,
                onValueChange = { speed = it },
                onValueChangeFinished = { FirePrefs.saveRDSpeed(context, speed) },
                valueRange = 1f..4f,
                steps = 2
            )
        }
    }
    var lichtOn by remember { mutableStateOf(FirePrefs.getLichtEnabled(context)) }
    var lichtInt by remember { mutableFloatStateOf(FirePrefs.getLichtIntensity(context)) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.licht_label), modifier = Modifier.weight(1f))
        Switch(
            checked = lichtOn,
            onCheckedChange = {
                lichtOn = it
                FirePrefs.saveLichtEnabled(context, it)
                onChanged()
            }
        )
    }
    if (lichtOn) {
        Column {
            Text(stringResource(R.string.licht_intensity_label, (lichtInt * 100).toInt()))
            Slider(
                value = lichtInt,
                onValueChange = { lichtInt = it },
                onValueChangeFinished = {
                    FirePrefs.saveLichtIntensity(context, lichtInt)
                    onChanged()
                },
                valueRange = 0f..1f
            )
        }
    }
}

