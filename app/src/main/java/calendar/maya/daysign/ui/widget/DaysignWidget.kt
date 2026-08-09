package calendar.maya.daysign.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import calendar.maya.daysign.MainActivity
import calendar.maya.daysign.R
import calendar.maya.daysign.data.AppDatabase
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.MayaDate
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DaysignWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.peopleDao()
        
        val date = LocalDate.now()
        val maya = MayaCalendar.maya(date)
        
        val allPeople = dao.getAllPeople().first()
        val birthdayPeople = allPeople.filter { person ->
            val personMaya = if (person.sunrise == "before") {
                MayaCalendar.maya(person.birthDate.minusDays(1))
            } else {
                MayaCalendar.maya(person.birthDate)
            }
            personMaya.kin == maya.kin
        }.map { it.name }

        provideContent {
            val prefs = currentState<Preferences>()
            WidgetContent(maya, birthdayPeople, prefs, context)
        }
    }

    @Composable
    private fun WidgetContent(maya: MayaDate, people: List<String>, prefs: Preferences, context: Context) {
        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        
        val bgColorLong = prefs[WidgetConfigKeys.backgroundColor] ?: Color.White.toArgb().toLong()
        val bgColorInt = bgColorLong.toInt()
        val bgColor = Color(bgColorInt)
        val bgAlpha = prefs[WidgetConfigKeys.backgroundAlpha] ?: 1f
        val showPhantoms = prefs[WidgetConfigKeys.showPhantoms] ?: true
        val showKin = prefs[WidgetConfigKeys.showKin] ?: true

        // Calculate luminance to determine text color
        val r = bgColor.red * bgAlpha + 1f * (1f - bgAlpha)
        val g = bgColor.green * bgAlpha + 1f * (1f - bgAlpha)
        val b = bgColor.blue * bgAlpha + 1f * (1f - bgAlpha)
        val luminance = 0.299f * r + 0.587f * g + 0.114f * b
        
        val isDark = luminance < 0.5f
        val contentColor = if (isDark) Color.White else Color.Black
        val secondaryContentColor = if (isDark) Color.LightGray else Color.DarkGray
        val tertiaryContentColor = if (isDark) Color.Gray else Color(0xFF757575)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(ColorProvider(bgColor.copy(alpha = bgAlpha)))
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "↻",
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()),
                    style = TextStyle(fontSize = 14.sp, color = ColorProvider(tertiaryContentColor))
                )
            }

            if (showKin) {
                Text(
                    text = "${context.getString(R.string.kin_label)} ${maya.kin}",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorProvider(contentColor)
                    )
                )
            }
            
            Row(
                modifier = GlanceModifier.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignImage(maya.daysign, 56.dp)
                Spacer(GlanceModifier.width(16.dp))
                SignImage(maya.trecena, 56.dp)
            }
            
            if (showPhantoms && maya.fantoms.isNotEmpty()) {
                Row(modifier = GlanceModifier.padding(top = 2.dp)) {
                    maya.fantoms.forEach { f ->
                        SignImage(f, 20.dp)
                        Spacer(GlanceModifier.width(4.dp))
                    }
                }
            }
            
            if (people.isNotEmpty()) {
                Text(
                    text = people.joinToString(", "),
                    style = TextStyle(
                        fontSize = 12.sp, 
                        color = ColorProvider(secondaryContentColor),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(top = 8.dp)
                )
            }

            Text(
                text = context.getString(R.string.widget_updated_at, currentTime),
                style = TextStyle(fontSize = 8.sp, color = ColorProvider(tertiaryContentColor)),
                modifier = GlanceModifier.padding(top = 4.dp)
            )
        }
    }

    @Composable
    private fun SignImage(sign: Int, size: androidx.compose.ui.unit.Dp) {
        val resId = getSignResId(sign)
        Image(
            provider = ImageProvider(resId),
            contentDescription = null,
            modifier = GlanceModifier.size(size)
        )
    }

    private fun getSignResId(sign: Int): Int {
        return when (sign) {
            1 -> R.drawable.ic_sign_1
            2 -> R.drawable.ic_sign_2
            3 -> R.drawable.ic_sign_3
            4 -> R.drawable.ic_sign_4
            5 -> R.drawable.ic_sign_5
            6 -> R.drawable.ic_sign_6
            7 -> R.drawable.ic_sign_7
            8 -> R.drawable.ic_sign_8
            9 -> R.drawable.ic_sign_9
            10 -> R.drawable.ic_sign_10
            11 -> R.drawable.ic_sign_11
            12 -> R.drawable.ic_sign_12
            13 -> R.drawable.ic_sign_13
            14 -> R.drawable.ic_sign_14
            15 -> R.drawable.ic_sign_15
            16 -> R.drawable.ic_sign_16
            17 -> R.drawable.ic_sign_17
            18 -> R.drawable.ic_sign_18
            19 -> R.drawable.ic_sign_19
            20 -> R.drawable.ic_sign_20
            else -> R.drawable.ic_launcher_foreground
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        DaysignWidget().updateAll(context)
    }
}

class DaysignWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysignWidget()
}
