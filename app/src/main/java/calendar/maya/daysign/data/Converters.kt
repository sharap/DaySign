package calendar.maya.daysign.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromList(value: List<Int>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String?): List<Int>? {
        return value?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() }
    }
}
