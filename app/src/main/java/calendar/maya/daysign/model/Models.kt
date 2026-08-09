package calendar.maya.daysign.model

import java.time.LocalDate

data class LongCount(
    val baktun: Int,
    val katun: Int,
    val tun: Int,
    val uinal: Int,
    val day: Int
)

data class MayaDate(
    val date: LocalDate,
    val kin: Int,
    val daysign: Int,
    val trecena: Int,
    val tone: Int,
    val fantoms: List<Int>,
    val longCount: LongCount
)

data class Person(
    val id: Int = 0,
    val name: String,
    val birthDate: LocalDate,
    val gender: String, // "male" or "female"
    val sunrise: String, // "before" or "after"
    val mayaDate: MayaDate? = null
)

data class Group(
    val id: Int = 0,
    val name: String,
    val description: String,
    val memberIds: List<Int>
)
