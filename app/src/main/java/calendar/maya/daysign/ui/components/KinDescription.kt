package calendar.maya.daysign.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.logic.KinDescriptionProvider

@Composable
fun KinDescription(
    kin: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    val description = KinDescriptionProvider.getDescription(kin, lang)
    Text(
        text = description,
        fontSize = 16.sp,
        modifier = modifier
    )
}
