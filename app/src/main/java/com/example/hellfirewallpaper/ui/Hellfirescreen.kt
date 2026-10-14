package com.example.hellfirewallpaper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.ui.Modifier
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hellfirewallpaper.FirePreview
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R

enum class Seccion(val titulo: Int) {
    FUEGO(R.string.tab_fuego),
    HIELO(R.string.tab_hielo),
    PATRONES(R.string.tab_patrones),
    IMAGEN(R.string.tab_imagen),
    AVANZADO(R.string.tab_avanzado),
    SISTEMA(R.string.tab_sistema)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HellFireScreen() {
    val context = LocalContext.current
    var seccion by rememberSaveable { mutableStateOf(0) }
    // Al cambiar cualquier ajuste se incrementa, y la pantalla se recompone
    var version by remember { mutableIntStateOf(0) }
    val onChanged: () -> Unit = { version++ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Leer version aquí fuerza la recomposición del preview
        val v = version

        FirePreview(
            hue = FirePrefs.getHue(context).toFloat(),
            decay = FirePrefs.getDecay(context).toFloat(),
            wind = FirePrefs.getWind(context),
            scale = FirePrefs.getScale(context),
            fireMode = FirePrefs.getMode(context),
            smokeEnabled = FirePrefs.getSmokeEnabled(context),
            smokeDensity = FirePrefs.getSmokeDensity(context),
            brightness = FirePrefs.getBrightness(context) / 255f,
            transparency = FirePrefs.getTransparency(context) / 255f,
            rainLength = FirePrefs.getRainLength(context),
            snowDensity = FirePrefs.getSnowDensity(context),
            fallSpeed = FirePrefs.getFallSpeed(context)
        )

        TabRow(
            selectedTabIndex = seccion,
            containerColor = Color.Black,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Seccion.entries.forEachIndexed { i, s ->
                Tab(
                    selected = seccion == i,
                    onClick = { seccion = i },
                    text = { Text(stringResource(s.titulo), maxLines = 1) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp)
        ) {
            when (Seccion.entries[seccion]) {
                Seccion.FUEGO -> SeccionFuego(context, onChanged)
                Seccion.HIELO -> SeccionHielo(context, onChanged)
                Seccion.PATRONES -> SeccionPatrones(onChanged)
                Seccion.IMAGEN -> SeccionImagen(onChanged)
                Seccion.AVANZADO -> SeccionAvanzado(context, onChanged)
                Seccion.SISTEMA -> SeccionSistema(context)
            }
        }
    }
}