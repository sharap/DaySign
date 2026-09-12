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

    private const val CORR = 584283 // Thompson Goodman Martinez Offset

    /** Day count since the correlation date - the basis for everything below. */
    private fun dayCount(date: LocalDate): Int =
        julian(date.year, date.monthValue, date.dayOfMonth) - CORR

    /** Position in the 260-day cycle, floor-mod so ancient dates stay in range. */
    private fun cycleIndex(days: Int): Int {
        val m = days % 260
        return if (m < 0) m + 260 else m
    }

    /**
     * Everything that repeats with the 260-day count. The long count and the date
     * itself are not cyclic, so they stay in MayaDate and are computed per date.
     */
    class CycleInfo(
        val kin: Int,
        val daysign: Int,
        val trecena: Int,
        val tone: Int,
        val fantoms: List<Int>
    )

    /**
     * The cyclic values for a date, without building a MayaDate.
     *
     * The returned object is shared, not allocated per call, so screens that only
     * need kin/daysign/trecena (the calendar list) can ask for a date's values on
     * every visible row without allocating anything.
     */
    fun cycleInfo(date: LocalDate): CycleInfo = cycleTable[cycleIndex(dayCount(date))]

    fun maya(date: LocalDate): MayaDate {
        val days = dayCount(date)
        val cycle = cycleTable[cycleIndex(days)]

        // Long Count - not cyclic, so still computed per date.
        var tempDays = days
        val baktun = floor(tempDays / 144000.0).toInt()
        tempDays -= baktun * 144000
        val katun = floor(tempDays / 7200.0).toInt()
        tempDays -= katun * 7200
        val tun = floor(tempDays / 360.0).toInt()
        tempDays -= tun * 360
        val uinal = floor(tempDays / 20.0).toInt()
        val day = cycle.kin % 20

        return MayaDate(
            date = date,
            kin = cycle.kin,
            daysign = cycle.daysign,
            trecena = cycle.trecena,
            tone = cycle.tone,
            fantoms = cycle.fantoms,
            longCount = LongCount(baktun, katun, tun, uinal, day),
        )
    }

    /**
     * Connections and phantoms are pure functions of the sign numbers, so the
     * results are computed once and shared. They are read on every frame of the
     * connection circle and for every visible calendar row, where recomputing
     * them allocated a fresh list each time.
     *
     * Index 0 is unused (signs are 1..20) and holds an empty list.
     */
    private val connectionsCache: Array<List<Int>> = Array(21) { sign ->
        if (sign in 1..20) computeConnections(sign) else emptyList()
    }

    private val fantomsCache: Array<List<Int>> = Array(21 * 21) { i ->
        computeFantoms(i / 21, i % 21)
    }

    /**
     * One entry per position in the 260-day cycle, built once. Declared after the
     * caches above because it reads getFantoms() while initialising.
     */
    private val cycleTable: Array<CycleInfo> = Array(260) { mod ->
        var kin = mod - 100
        if (kin < 1) kin += 260
        val tone = if (kin % 13 == 0) 13 else kin % 13
        val daysign = if (kin % 20 == 0) 20 else kin % 20
        val trecena = if ((daysign - tone + 1) < 1) (daysign - tone + 21) else (daysign - tone + 1)
        CycleInfo(kin, daysign, trecena, tone, getFantoms(daysign, trecena))
    }

    fun getConnections(daysign: Int): List<Int> =
        if (daysign in 1..20) connectionsCache[daysign] else emptyList()

    fun getFantoms(daysign: Int, trecena: Int): List<Int> =
        if (daysign in 0..20 && trecena in 0..20) fantomsCache[daysign * 21 + trecena]
        else computeFantoms(daysign, trecena)

    private fun computeConnections(daysign: Int): List<Int> {
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

    private fun computeFantoms(daysign: Int, trecena: Int): List<Int> {
        if (daysign == trecena) return emptyList()
        
        val fantoms = listOf(daysign, trecena)
        
        val conns = IntArray(21)
        for (f in fantoms) {
            val c = if (f in 1..20) computeConnections(f) else emptyList()
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
