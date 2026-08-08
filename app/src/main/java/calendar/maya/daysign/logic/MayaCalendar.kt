package calendar.maya.daysign.logic

import calendar.maya.daysign.model.LongCount
import calendar.maya.daysign.model.MayaDate
import java.time.LocalDate
import kotlin.math.floor

object MayaCalendar {

    val activator = listOf(
        1, 20, 22, 39, 43, 50, 51, 58, 64, 69, 72, 77, 85, 88, 93, 96, 106, 107, 108, 109, 110, 
        111, 112, 113, 114, 115, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 165, 168, 173, 
        176, 184, 189, 192, 197, 203, 210, 211, 218, 222, 239, 241, 260
    )

    fun julian(y: Int, m: Int, d: Int): Int {
        var year = y
        var month = m
        if (month < 3) {
            month += 12
            year -= 1
        }
        val a = floor(year / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        return floor(365.25 * (year + 4716.0)).toInt() + floor(30.6001 * (month + 1)).toInt() + d + b - 1524
    }

    fun maya(date: LocalDate): MayaDate {
        val corr = 584283 // Thompson Goodman Martinez Offset
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth

        val julianDays = julian(y, m, d)
        val days = julianDays - corr
        
        var kin = days - (260 * floor(days / 260.0).toInt()) - 100
        if (kin < 1) kin += 260

        // Long Count
        var tempDays = days
        val baktun = floor(tempDays / 144000.0).toInt()
        tempDays -= baktun * 144000
        val katun = floor(tempDays / 7200.0).toInt()
        tempDays -= katun * 7200
        val tun = floor(tempDays / 360.0).toInt()
        tempDays -= tun * 360
        val uinal = floor(tempDays / 20.0).toInt()
        val day = kin % 20

        val tone = if (kin % 13 == 0) 13 else kin % 13
        val daysign = if (kin % 20 == 0) 20 else kin % 20
        
        val trecena = if ((daysign - tone + 1) < 1) (daysign - tone + 21) else (daysign - tone + 1)
        
        return MayaDate(
            date = date,
            kin = kin,
            daysign = daysign,
            trecena = trecena,
            tone = tone,
            fantoms = getFantoms(daysign, trecena),
            longCount = LongCount(baktun, katun, tun, uinal, day),
        )
    }

    fun getConnections(daysign: Int): List<Int> {
        if (daysign !in 1..20) return emptyList()
        
        val connections = mutableListOf<Int>()
        connections.add(if ((daysign + 8) > 20) (daysign + 8 - 20) else (daysign + 8)) // ты поддерживаешь
        connections.add(if ((daysign - 8) < 1) (20 + (daysign - 8)) else (daysign - 8)) // тебя поддерживает
        connections.add(if ((daysign + 6) > 20) (daysign + 6 - 20) else (daysign + 6)) // ты контролируешь
        connections.add(if ((daysign - 6) < 1) (20 + (daysign - 6)) else (daysign - 6)) // тебя контролирует
        connections.add(if ((daysign + 10) > 20) (daysign - 10) else (daysign + 10)) // страсть/испытание
        connections.add(if ((19 - daysign) < 1) (19 - daysign + 20) else (19 - daysign)) // партнёр/друг
        connections.add(21 - daysign) // тайная сила
        
        return connections
    }

    fun getFantoms(daysign: Int, trecena: Int): List<Int> {
        if (daysign == trecena) return emptyList()
        
        val fantoms = listOf(daysign, trecena)
        
        val conns = IntArray(21)
        for (f in fantoms) {
            val c = getConnections(f)
            for (conn in c) {
                conns[conn]++
            }
        }
        
        val fns = mutableListOf<Int>()
        // Reset counts for the daysign and trecena themselves (following maya.js logic)
        for (f in fantoms) {
            conns[f] = 0
        }
        
        for (i in 1 until conns.size) {
            if (conns[i] >= 2) {
                fns.add(i)
            }
        }
        return fns
    }

    fun getKinConnections(
        kin1Daysign: Int,
        kin1Trecena: Int,
        kin2Daysign: Int,
        kin2Trecena: Int
    ): List<Int> {
        val allCons1 = listOf(getConnections(kin1Daysign), getConnections(kin1Trecena))
        val signs2 = listOf(kin2Daysign, kin2Trecena)
        val conTypes = IntArray(9)

        for (cons in allCons1) {
            for (sign in signs2) {
                for (i in cons.indices) {
                    if (sign == cons[i]) {
                        conTypes[i]++
                    }
                }
            }
        }

        if (kin1Daysign == kin2Trecena || kin1Trecena == kin2Daysign) {
            conTypes[7]++
        }
        if (kin1Daysign == kin2Daysign || kin1Trecena == kin2Trecena) {
            conTypes[8]++
        }

        return conTypes.toList()
    }

    fun nextDay(date: LocalDate): LocalDate = date.plusDays(1)
    fun backDay(date: LocalDate): LocalDate = date.minusDays(1)
}
