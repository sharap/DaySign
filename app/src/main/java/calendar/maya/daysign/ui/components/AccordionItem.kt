package calendar.maya.daysign.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AccordionItem(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!visible) return

    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
            trailingContent = {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable { onToggle() }
        )
        AnimatedVisibility(visible = isExpanded) {
            content()
        }
        HorizontalDivider()
    }
}
