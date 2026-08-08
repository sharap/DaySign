package calendar.maya.daysign.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R

fun getSignColor(s: Int): Color {
    return when (s % 4) {
        1 -> Color(0xFFEF9A9A) // South/Red
        2 -> Color(0xFFEEEEEE) // North/White
        3 -> Color(0xFF90CAF9) // West/Blue
        0 -> Color(0xFFFFF59D) // East/Yellow
        else -> Color.Gray
    }
}

@Composable
fun SignChips(daysign: Int, trecena: Int, modifier: Modifier = Modifier) {
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = { },
            label = { Text("${daysignNames.getOrElse(daysign) { "" }} ($daysign)", fontSize = 12.sp) },
            leadingIcon = { ImageSign(sign = daysign, size = 20.dp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = getSignColor(daysign).copy(alpha = 0.3f)
            )
        )
        AssistChip(
            onClick = { },
            label = { Text("${daysignNamesTo.getOrElse(trecena) { "" }} ($trecena)", fontSize = 12.sp) },
            leadingIcon = { ImageSign(sign = trecena, size = 20.dp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = getSignColor(trecena).copy(alpha = 0.3f)
            )
        )
    }
}
