package calendar.maya.daysign.logic

import calendar.maya.daysign.logic.descriptions.kinDescriptionsEN
import calendar.maya.daysign.logic.descriptions.kinDescriptionsRU

object KinDescriptionProvider {
    fun getDescription(kin: Int, lang: String): String {
        return if (lang == "ru") {
            kinDescriptionsRU[kin] ?: "Описание кина $kin еще не добавлено."
        } else {
            kinDescriptionsEN[kin] ?: "Description for kin $kin is not available yet."
        }
    }
}
