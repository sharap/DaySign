package calendar.maya.daysign.ui.components

import androidx.annotation.DrawableRes
import calendar.maya.daysign.R

/**
 * Static mapping from a character id to its photo resource.
 *
 * Replaces Resources.getIdentifier(), which resolves resources by name at runtime.
 * That lookup is slow, defeats resource shrinking, and was being called on every
 * recomposition of the character detail screen.
 *
 * The photos live in res/drawable-nodpi/ so the framework decodes them at their
 * native size instead of upscaling them to the screen density.
 */
@DrawableRes
fun characterPhotoRes(characterId: Int): Int? = when (characterId) {
    1 -> R.drawable.photo1
    2 -> R.drawable.photo2
    3 -> R.drawable.photo3
    4 -> R.drawable.photo4
    5 -> R.drawable.photo5
    6 -> R.drawable.photo6
    7 -> R.drawable.photo7
    8 -> R.drawable.photo8
    9 -> R.drawable.photo9
    10 -> R.drawable.photo10
    11 -> R.drawable.photo11
    12 -> R.drawable.photo12
    13 -> R.drawable.photo13
    14 -> R.drawable.photo14
    15 -> R.drawable.photo15
    16 -> R.drawable.photo16
    17 -> R.drawable.photo17
    18 -> R.drawable.photo18
    19 -> R.drawable.photo19
    20 -> R.drawable.photo20
    else -> null
}
