package com.example.hellfirewallpaper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionSistema(context: Context) {
    var lang by remember { mutableStateOf(FirePrefs.getLang(context)) }
    val idiomas = listOf(
        "" to R.string.lang_auto,
        "es" to R.string.lang_es,
        "en" to R.string.lang_en
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        idiomas.forEach { (code, res) ->
            FilterChip(
                selected = lang == code,
                onClick = {
                    lang = code
                    FirePrefs.saveLang(context, code)
                    // Recrea la actividad para aplicar el idioma
                    (context as? Activity)?.recreate()
                },
                label = { Text(stringResource(res)) }
            )
        }
    }

    Text(stringResource(R.string.notif_listener_desc), modifier = Modifier.padding(top = 8.dp))

    Button(
        onClick = {
            try {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (_: Exception) {
                try {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(context, R.string.notif_manual_hint, Toast.LENGTH_LONG).show()
                }
            }
        },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(stringResource(R.string.notif_listener_title))
    }
}

