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

/**
 * Natural width/height ratio of each character photo.
 *
 * The photos are decoded asynchronously, so the layout has no intrinsic size to
 * measure against until the bitmap arrives. Reserving the exact ratio up front
 * keeps the slot the same height it had with the synchronous painterResource()
 * load, so nothing jumps or gets cropped while the image is decoding.
 *
 * These must stay in sync with the files in res/drawable-nodpi/.
 */
fun characterPhotoAspectRatio(characterId: Int): Float = when (characterId) {
    1 -> 788f / 1126f // 788x1126
    2 -> 777f / 1102f // 777x1102
    3 -> 762f / 1106f // 762x1106
    4 -> 765f / 1122f // 765x1122
    5 -> 756f / 1116f // 756x1116
    6 -> 783f / 1136f // 783x1136
    7 -> 774f / 1118f // 774x1118
    8 -> 793f / 1113f // 793x1113
    9 -> 794f / 1122f // 794x1122
    10 -> 790f / 1141f // 790x1141
    11 -> 784f / 1138f // 784x1138
    12 -> 802f / 1141f // 802x1141
    13 -> 738f / 1131f // 738x1131
    14 -> 770f / 1128f // 770x1128
    15 -> 780f / 1117f // 780x1117
    16 -> 778f / 1113f // 778x1113
    17 -> 789f / 1133f // 789x1133
    18 -> 776f / 1033f // 776x1033
    19 -> 754f / 1121f // 754x1121
    20 -> 768f / 1120f // 768x1120
    else -> 0.7f
}
