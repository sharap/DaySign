package calendar.maya.daysign.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import calendar.maya.daysign.R
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            WidgetConfigScreen(
                onSave = { color, alpha, phantoms, kin ->
                    saveConfig(color, alpha, phantoms, kin)
                },
                onCancel = { finish() }
            )
        }
    }

    private fun saveConfig(color: Color, alpha: Float, phantoms: Boolean, kin: Boolean) {
        val scope = MainScope()
        scope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@WidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WidgetConfigKeys.backgroundColor] = color.toArgb().toLong()
                    this[WidgetConfigKeys.backgroundAlpha] = alpha
                    this[WidgetConfigKeys.showPhantoms] = phantoms
                    this[WidgetConfigKeys.showKin] = kin
                }
            }
            DaysignWidget().update(this@WidgetConfigActivity, glanceId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}

@Composable
fun WidgetConfigScreen(
    onSave: (Color, Float, Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.White) }
    var alpha by remember { mutableStateOf(1f) }
    var showPhantoms by remember { mutableStateOf(true) }
    var showKin by remember { mutableStateOf(true) }

    val colors = listOf(
        Color.White, Color.Black, Color(0xFFF5F5F5), Color(0xFFE0E0E0),
        Color(0xFFEF9A9A), Color(0xFF90CAF9), Color(0xFFFFF59D), Color(0xFFA5D6A7)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.widget_config_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text(stringResource(R.string.widget_bg_color), style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { selectedColor = color }
                        .then(
                            if (selectedColor == color) Modifier.background(color.copy(alpha = 0.5f))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == color) {
                        RadioButton(selected = true, onClick = null)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.widget_transparency, (alpha * 100).toInt()))
        Slider(
            value = alpha,
            onValueChange = { alpha = it },
            valueRange = 0.1f..1f
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPhantoms = !showPhantoms },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = showPhantoms, onCheckedChange = { showPhantoms = it })
            Text(stringResource(R.string.widget_show_phantoms))
        }

        Row(
            modifier = Modifier.fillMaxWidth().clickable { showKin = !showKin },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = showKin, onCheckedChange = { showKin = it })
            Text(stringResource(R.string.widget_show_kin))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onSave(selectedColor, alpha, showPhantoms, showKin) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}
