package calendar.maya.daysign.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

/**
 * Number of rows a long, eagerly-composed list should render right now, growing
 * by one chunk per frame until the whole list is on screen.
 *
 * These lists sit inside verticalScroll() containers and inside LazyColumn items,
 * where a nested LazyColumn cannot be measured, so every row has to be composed
 * eagerly. Composing a hundred rows in a single frame is what makes expanding a
 * section hitch. The total work is unchanged - it is just spread across a few
 * frames, so no single frame blows its budget and the expand animation stays
 * smooth.
 *
 * The count resets whenever [total] changes, so a new list starts from the top.
 */
@Composable
fun rememberProgressiveCount(total: Int, initial: Int = 24, step: Int = 24): Int {
    val count = remember(total) { mutableIntStateOf(minOf(initial, total)) }

    LaunchedEffect(total) {
        while (count.intValue < total) {
            withFrameNanos { }
            count.intValue = minOf(count.intValue + step, total)
        }
    }

    return count.intValue
}
