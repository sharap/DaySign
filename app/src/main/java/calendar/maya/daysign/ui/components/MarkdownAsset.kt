package calendar.maya.daysign.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.delay
import java.io.InputStreamReader

@Composable
fun MarkdownAsset(path: String) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    LaunchedEffect(path) {
        try {
            context.assets.open(path).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    text = reader.readText()
                }
            }
        } catch (e: Exception) {
            text = "Описание не найдено"
        }
    }
    if (text.isNotEmpty()) {
        Column {
            Markdown(content = text)
            Spacer(modifier = Modifier.height(8.dp))
            CopyButton(text = text)
        }
    }
}

@Composable
fun CopyButton(text: String) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    AssistChip(
        onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Daysign", text)
            clipboard.setPrimaryClip(clip)
            isCopied = true
            Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
        },
        label = { Text(if (isCopied) "Скопировано" else "Копировать") },
        leadingIcon = {
            Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = Modifier.padding(top = 8.dp)
    )

    if (isCopied) {
        LaunchedEffect(Unit) {
            delay(2000)
            isCopied = false
        }
    }
}
