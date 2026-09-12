package calendar.maya.daysign.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import calendar.maya.daysign.R

/**
 * Sign artwork above this many pixels keeps using the vector directly.
 *
 * Those are the hero images (120-200 dp): one or two per screen, so a single
 * rasterization is not what causes jank, and caching them would cost megabytes.
 */
private const val MAX_RASTER_PX = 256

/**
 * Rasterized sign artwork, shared by every composable that draws the same sign
 * at the same pixel size.
 *
 * The sign vectors are heavy - some carry a single path with ~12 000 characters
 * of path data. Compose parses the ImageVector once, but every Image() gets its
 * own VectorPainter and rasterizes those paths into its own layer, so a list
 * showing two signs per row re-rendered them continuously while scrolling.
 *
 * The bitmap is produced by Android's own VectorDrawable renderer at exactly the
 * size it will be drawn at, so the result is pixel-identical to drawing the
 * vector - this trades memory for work, not quality.
 */
private object SignBitmapCache {

    private val cache = object : LruCache<Long, ImageBitmap>(6 * 1024 * 1024) {
        override fun sizeOf(key: Long, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun get(context: Context, resId: Int, sizePx: Int): ImageBitmap? {
        val key = resId.toLong() shl 20 or sizePx.toLong()
        cache.get(key)?.let { return it }

        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(android.graphics.Canvas(bitmap))

        return bitmap.asImageBitmap().also { cache.put(key, it) }
    }
}

@androidx.annotation.DrawableRes
private fun signDrawableRes(sign: Int): Int = when (sign) {
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

@Composable
fun ImageSign(
    sign: Int,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val resId = signDrawableRes(sign)
    val contentDescription = "Daysign $sign"
    val sizePx = with(LocalDensity.current) { size.roundToPx() }

    // The sign vectors are square (512x512 viewport), so a square bitmap of the
    // laid-out size maps onto them 1:1 without distorting anything.
    val bitmap = if (sizePx in 1..MAX_RASTER_PX) {
        val context = LocalContext.current
        remember(resId, sizePx) { SignBitmapCache.get(context, resId, sizePx) }
    } else {
        null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    } else {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    }
}
