package calendar.maya.daysign.ui.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object WidgetConfigKeys {
    val backgroundColor = longPreferencesKey("backgroundColor")
    val backgroundAlpha = floatPreferencesKey("backgroundAlpha")
    val showPhantoms = booleanPreferencesKey("showPhantoms")
    val showKin = booleanPreferencesKey("showKin")
}
