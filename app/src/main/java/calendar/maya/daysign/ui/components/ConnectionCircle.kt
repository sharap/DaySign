package calendar.maya.daysign.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign
import kotlin.math.PI

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectionCircle(
    daysign: Int,
    trecena: Int,
    modifier: Modifier = Modifier
) {
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val circlePositions = listOf(11, 10, 9, 12, 7, 14, 5, 16, 3, 18, 1, 20, 19, 2, 17, 4, 15, 6, 13, 8)
    
    var signList by remember { mutableStateOf(if (daysign == trecena) listOf(daysign) else listOf(daysign, trecena)) }
    var supportEnabled by remember { mutableStateOf(true) }
    var controlEnabled by remember { mutableStateOf(true) }
    var passionEnabled by remember { mutableStateOf(true) }
    var friendEnabled by remember { mutableStateOf(true) }
    var secretEnabled by remember { mutableStateOf(true) }
    var minConn by remember { mutableIntStateOf(1) }

    val signTextPaint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    val intersections = IntArray(21)
    val visibleSigns = signList.toMutableSet()

    fun getSignColor(s: Int): Color {
        return when (s % 4) {
            1 -> Color(0xFFEF9A9A) // South/Red
            2 -> Color(0xFFEEEEEE) // North/White (light grey for visibility)
            3 -> Color(0xFF90CAF9) // West/Blue
            0 -> Color(0xFFFFF59D) // East/Yellow
            else -> Color.Gray
        }
    }

    fun getSignTextColor(s: Int): Int {
        return when (s % 4) {
            1, 3 -> android.graphics.Color.WHITE
            else -> android.graphics.Color.BLACK
        }
    }

    signList.forEach { sign ->
        intersections[sign] = 7 // Source sign always visible
        val conns = MayaCalendar.getConnections(sign)
        conns.forEachIndexed { index, conn ->
            if (conn in 1..20) {
                var enabled = false
                when (index) {
                    0, 1 -> if (supportEnabled) { intersections[conn]++; enabled = true }
                    2, 3 -> if (controlEnabled) { intersections[conn]++; enabled = true }
                    4 -> if (passionEnabled) { intersections[conn]++; enabled = true }
                    5 -> if (friendEnabled) { intersections[conn]++; enabled = true }
                    6 -> if (secretEnabled) { intersections[conn]++; enabled = true }
                }
                if (enabled && intersections[conn] >= minConn) {
                    visibleSigns.add(conn)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // ... (Toggles and Chips stay same)
        // Toggles
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(selected = supportEnabled, onClick = { supportEnabled = !supportEnabled }, label = { Text(stringResource(R.string.support)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50), selectedLabelColor = Color.White))
            FilterChip(selected = controlEnabled, onClick = { controlEnabled = !controlEnabled }, label = { Text(stringResource(R.string.control)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2196F3), selectedLabelColor = Color.White))
            FilterChip(selected = passionEnabled, onClick = { passionEnabled = !passionEnabled }, label = { Text(stringResource(R.string.passion)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Red, selectedLabelColor = Color.White))
            FilterChip(selected = friendEnabled, onClick = { friendEnabled = !friendEnabled }, label = { Text(stringResource(R.string.friend)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC0CB), selectedLabelColor = Color.Black))
            FilterChip(selected = secretEnabled, onClick = { secretEnabled = !secretEnabled }, label = { Text(stringResource(R.string.secret)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Yellow, selectedLabelColor = Color.Black))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(stringResource(R.string.min_connections_label), fontSize = 12.sp)
            (1..5).forEach { i ->
                InputChip(
                    selected = minConn == i,
                    onClick = { minConn = i },
                    label = { Text(i.toString()) },
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        // Active Signs Chips
        FlowRow(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (signList.isEmpty()) {
                SuggestionChip(onClick = { signList = (1..20).toList() }, label = { Text(stringResource(R.string.all_signs)) })
            }
            signList.forEach { sign ->
                InputChip(
                    selected = true,
                    onClick = { signList = signList.filter { it != sign } },
                    label = { Text("${daysignNames.getOrElse(sign) { "" }} ($sign)") },
                    leadingIcon = { ImageSign(sign = sign, size = 20.dp) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = getSignColor(sign).copy(alpha = 0.3f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // The Circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            val radius = size.width / 2 * 0.9f
                            
                            // Check if tap is near the circle boundary where signs are
                            if (dist > radius * 0.6f && dist < radius * 1.2f) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                if (angle < 0) angle += 360f
                                
                                val signIndex = ((angle + 9f) / 18f).toInt() % 20
                                val tappedSign = circlePositions.getOrNull(signIndex)
                                tappedSign?.let { sign ->
                                    if (!signList.contains(sign)) {
                                        signList = signList + sign
                                    }
                                }
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 * 0.9f
                val signRadius = 15.dp.toPx()

                circlePositions.forEachIndexed { i, sign ->
                    val angle = i * 18f
                    val signPos = Offset(
                        center.x + radius * sin(angle * PI / 180f).toFloat(),
                        center.y - radius * cos(angle * PI / 180f).toFloat()
                    )
                    
                    val opacity = if (visibleSigns.contains(sign)) 1f else 0f
                    if (opacity == 0f) return@forEachIndexed

                    withTransform({
                        rotate(angle, center)
                    }) {
                        val localSignPos = Offset(center.x, center.y - radius)
                        val conns = MayaCalendar.getConnections(sign)
                        val sourceInList = signList.contains(sign)
                        
                        val gap = 3.dp.toPx() 
                        val effectiveSignRadius = signRadius + gap
                        val arrowSize = 6.dp.toPx()

                        // Passion (Red line through center) - Mutual
                        if (passionEnabled && sourceInList && intersections[conns[4]] >= minConn) {
                            val targetSign = conns[4]
                            if (sign < targetSign || !signList.contains(targetSign)) {
                                val linePos1 = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                val linePos2 = Offset(localSignPos.x, localSignPos.y + radius * 2 - effectiveSignRadius)
                                
                                drawLine(Color.Red, linePos1, linePos2, strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                
                                // Arrows at both ends
                                drawLine(Color.Red, linePos1, Offset(linePos1.x - arrowSize/2, linePos1.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                drawLine(Color.Red, linePos1, Offset(linePos1.x + arrowSize/2, linePos1.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                drawLine(Color.Red, linePos2, Offset(linePos2.x - arrowSize/2, linePos2.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                drawLine(Color.Red, linePos2, Offset(linePos2.x + arrowSize/2, linePos2.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                            }
                        }

                        // Support (Green) - Directional
                        if (supportEnabled && sourceInList) {
                            val chordLength144 = 2f * radius * sin(72f * PI.toFloat() / 180f)
                            val lineLength = chordLength144 - 2 * effectiveSignRadius
                            val rotAngle = 18f // Mathematically correct for 144 chord

                            // S supports Target (Outgoing)
                            if (intersections[conns[0]] >= minConn) {
                                val targetSign = conns[0]
                                withTransform({ rotate(if (sign % 2 != 0) rotAngle else -rotAngle, localSignPos) }) {
                                    val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                    val end = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius + lineLength)
                                    drawLine(Color(0xFF4CAF50), start, end, strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    
                                    // Outgoing arrow (at end)
                                    drawLine(Color(0xFF4CAF50), end, Offset(end.x - arrowSize/2, end.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    drawLine(Color(0xFF4CAF50), end, Offset(end.x + arrowSize/2, end.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                }
                            }
                            // Source is supported by Target (Incoming)
                            if (intersections[conns[1]] >= minConn) {
                                withTransform({ rotate(if (sign % 2 != 0) -rotAngle else rotAngle, localSignPos) }) {
                                    val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                    val end = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius + lineLength)
                                    drawLine(Color(0xFF4CAF50), start, end, strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    // Incoming arrow at the source sign
                                    drawLine(Color(0xFF4CAF50), start, Offset(start.x - arrowSize/2, start.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    drawLine(Color(0xFF4CAF50), start, Offset(start.x + arrowSize/2, start.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                }
                            }
                        }

                        // Control (Blue) - Directional
                        if (controlEnabled && sourceInList) {
                            val chordLength108 = 2f * radius * sin(54f * PI.toFloat() / 180f)
                            val lineLength = chordLength108 - 2 * effectiveSignRadius
                            val rotAngle = 36f

                            // S controls Target
                            if (intersections[conns[2]] >= minConn) {
                                withTransform({ rotate(if (sign % 2 != 0) rotAngle else -rotAngle, localSignPos) }) {
                                    val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                    val end = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius + lineLength)
                                    drawLine(Color(0xFF2196F3), start, end, strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    
                                    // Outgoing arrow
                                    drawLine(Color(0xFF2196F3), end, Offset(end.x - arrowSize/2, end.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    drawLine(Color(0xFF2196F3), end, Offset(end.x + arrowSize/2, end.y - arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                }
                            }
                            // Source is controlled by Target
                            if (intersections[conns[3]] >= minConn) {
                                withTransform({ rotate(if (sign % 2 != 0) -rotAngle else rotAngle, localSignPos) }) {
                                    val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                    val end = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius + lineLength)
                                    drawLine(Color(0xFF2196F3), start, end, strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    drawLine(Color(0xFF2196F3), start, Offset(start.x - arrowSize/2, start.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                    drawLine(Color(0xFF2196F3), start, Offset(start.x + arrowSize/2, start.y + arrowSize), strokeWidth = 1.dp.toPx(), alpha = 0.6f)
                                }
                            }
                        }

                        // Friend (Pink) - Adjacent
                        if (friendEnabled && sourceInList) {
                            val targetSign = if (sign % 2 != 0) conns[6] else conns[5]
                            if (intersections[targetSign] >= minConn) {
                                val targetInList = signList.contains(targetSign)
                                if (!targetInList || sign < targetSign) {
                                    withTransform({ rotate(-81f, localSignPos) }) {
                                        val chord18 = 2f * radius * sin(9f * PI.toFloat() / 180f)
                                        val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                        val end = Offset(localSignPos.x, localSignPos.y + chord18 - effectiveSignRadius)
                                        val color = if (sign % 2 != 0) Color.Yellow else Color(0xFFFFC0CB)
                                        drawLine(color, start, end, strokeWidth = 2.dp.toPx())
                                    }
                                }
                            }
                        }
                        
                        // Secret (Yellow) - Adjacent
                        if (secretEnabled && sourceInList) {
                            val targetSign = if (sign % 2 != 0) conns[5] else conns[6]
                            if (intersections[targetSign] >= minConn) {
                                val targetInList = signList.contains(targetSign)
                                if (!targetInList || sign < targetSign) {
                                    withTransform({ rotate(81f, localSignPos) }) {
                                        val chord18 = 2f * radius * sin(9f * PI.toFloat() / 180f)
                                        val start = Offset(localSignPos.x, localSignPos.y + effectiveSignRadius)
                                        val end = Offset(localSignPos.x, localSignPos.y + chord18 - effectiveSignRadius)
                                        val color = if (sign % 2 != 0) Color(0xFFFFC0CB) else Color.Yellow
                                        drawLine(color, start, end, strokeWidth = 2.dp.toPx())
                                    }
                                }
                            }
                        }
                    }

                        // The Sign Circle
                        drawCircle(
                            color = getSignColor(sign),
                            radius = signRadius,
                            center = signPos,
                            alpha = opacity
                        )
                        
                        // The paint is reused instead of allocated per sign per frame;
                        // drawing is sequential, so mutating it here is safe.
                        signTextPaint.color = getSignTextColor(sign)
                        signTextPaint.textSize = 12.dp.toPx()
                        drawContext.canvas.nativeCanvas.drawText(
                            sign.toString(),
                            signPos.x,
                            signPos.y + 5.dp.toPx(),
                            signTextPaint
                        )
                }
            }
        }
    }
}
