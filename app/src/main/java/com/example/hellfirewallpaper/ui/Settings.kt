package com.example.hellfirewallpaper.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchSetting(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    var c by remember(checked) { mutableStateOf(checked) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = c,
            onCheckedChange = {
                c = it
                onChange(it)
            }
        )
    }
}

@Composable
fun SliderSetting(
    label: (Float) -> String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onSave: (Float) -> Unit,
    steps: Int = 0
) {
    var v by remember(value) { mutableFloatStateOf(value) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label(v))
        Slider(
            value = v,
            onValueChange = { v = it },
            onValueChangeFinished = { onSave(v) },
            valueRange = range,
            steps = steps
        )
    }
}

