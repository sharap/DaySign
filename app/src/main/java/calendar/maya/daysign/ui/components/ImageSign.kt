package calendar.maya.daysign.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import calendar.maya.daysign.R

@Composable
fun ImageSign(
    sign: Int,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val resId = when (sign) {
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
        else -> R.drawable.ic_launcher_foreground // Placeholder
    }
    
    Image(
        painter = painterResource(id = resId),
        contentDescription = "Daysign $sign",
        modifier = modifier.size(size)
    )
}
