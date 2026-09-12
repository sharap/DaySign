package calendar.maya.daysign

import calendar.maya.daysign.logic.MayaCalendar
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Reference copies of the pre-memoization implementations. */
private fun refConnections(daysign: Int): List<Int> {
    if (daysign !in 1..20) return emptyList()
    val c = mutableListOf<Int>()
    c.add(if ((daysign + 8) > 20) (daysign + 8 - 20) else (daysign + 8))
    c.add(if ((daysign - 8) < 1) (20 + (daysign - 8)) else (daysign - 8))
    c.add(if ((daysign + 6) > 20) (daysign + 6 - 20) else (daysign + 6))
    c.add(if ((daysign - 6) < 1) (20 + (daysign - 6)) else (daysign - 6))
    c.add(if ((daysign + 10) > 20) (daysign - 10) else (daysign + 10))
    c.add(if ((19 - daysign) < 1) (19 - daysign + 20) else (19 - daysign))
    c.add(21 - daysign)
    return c
}

private fun refFantoms(daysign: Int, trecena: Int): List<Int> {
    if (daysign == trecena) return emptyList()
    val fantoms = listOf(daysign, trecena)
    val conns = IntArray(21)
    for (f in fantoms) for (conn in refConnections(f)) conns[conn]++
    for (f in fantoms) conns[f] = 0
    val fns = mutableListOf<Int>()
    for (i in 1 until conns.size) if (conns[i] >= 2) fns.add(i)
    return fns
}

class MayaCalendarCacheTest {

    @Test
    fun connectionsMatchReference() {
        for (sign in -5..25) {
            assertEquals("sign=$sign", refConnections(sign), MayaCalendar.getConnections(sign))
        }
    }

    @Test
    fun fantomsMatchReference() {
        for (d in 0..20) for (t in 0..20) {
            assertEquals("d=$d t=$t", refFantoms(d, t), MayaCalendar.getFantoms(d, t))
        }
    }

    @Test
    fun mayaDatesUnchangedOverFullCycle() {
        // Two full 260-day cycles plus leap-year coverage.
        var date = LocalDate.of(2023, 1, 1)
        repeat(800) {
            val m = MayaCalendar.maya(date)
            assertEquals("date=$date", refFantoms(m.daysign, m.trecena), m.fantoms)
            date = date.plusDays(1)
        }
    }

    @Test
    fun cachedListsAreStableInstances() {
        assertEquals(true, MayaCalendar.getConnections(7) === MayaCalendar.getConnections(7))
        assertEquals(true, MayaCalendar.getFantoms(3, 11) === MayaCalendar.getFantoms(3, 11))
    }
}

/** Reference copy of the pre-table maya() implementation. */
private fun refMaya(date: LocalDate): List<Int> {
    val corr = 584283
    val julianDays = MayaCalendar.julian(date.year, date.monthValue, date.dayOfMonth)
    val days = julianDays - corr

    var kin = days - (260 * Math.floor(days / 260.0).toInt()) - 100
    if (kin < 1) kin += 260

    var tempDays = days
    val baktun = Math.floor(tempDays / 144000.0).toInt()
    tempDays -= baktun * 144000
    val katun = Math.floor(tempDays / 7200.0).toInt()
    tempDays -= katun * 7200
    val tun = Math.floor(tempDays / 360.0).toInt()
    tempDays -= tun * 360
    val uinal = Math.floor(tempDays / 20.0).toInt()
    val day = kin % 20

    val tone = if (kin % 13 == 0) 13 else kin % 13
    val daysign = if (kin % 20 == 0) 20 else kin % 20
    val trecena = if ((daysign - tone + 1) < 1) (daysign - tone + 21) else (daysign - tone + 1)

    return listOf(kin, daysign, trecena, tone, baktun, katun, tun, uinal, day)
}

private fun actualMaya(date: LocalDate): List<Int> {
    val m = MayaCalendar.maya(date)
    return listOf(
        m.kin, m.daysign, m.trecena, m.tone,
        m.longCount.baktun, m.longCount.katun, m.longCount.tun, m.longCount.uinal, m.longCount.day
    )
}

class MayaCalendarTableTest {

    /** Every day across two centuries must be untouched by the lookup table. */
    @Test
    fun mayaMatchesReferenceAcrossTwoCenturies() {
        var date = LocalDate.of(1900, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        var checked = 0
        while (!date.isAfter(end)) {
            assertEquals("date=$date", refMaya(date), actualMaya(date))
            assertEquals("fantoms date=$date", refFantoms(
                refMaya(date)[1], refMaya(date)[2]
            ), MayaCalendar.maya(date).fantoms)
            date = date.plusDays(1)
            checked++
        }
        assertEquals(73414, checked) // 201 years, 49 leap days
    }

    /** Dates before the correlation date make the day count negative. */
    @Test
    fun mayaMatchesReferenceOnAncientDates() {
        for (year in intArrayOf(-3200, -3114, -1000, 1, 500, 1583)) {
            var date = LocalDate.of(year, 1, 1)
            repeat(400) {
                assertEquals("date=$date", refMaya(date), actualMaya(date))
                date = date.plusDays(1)
            }
        }
    }

    /** cycleInfo() must agree with maya() and hand back the shared instance. */
    @Test
    fun cycleInfoMatchesMayaAndIsShared() {
        var date = LocalDate.of(2024, 1, 1)
        repeat(1000) {
            val full = MayaCalendar.maya(date)
            val cyclic = MayaCalendar.cycleInfo(date)
            assertEquals("kin date=$date", full.kin, cyclic.kin)
            assertEquals("daysign date=$date", full.daysign, cyclic.daysign)
            assertEquals("trecena date=$date", full.trecena, cyclic.trecena)
            assertEquals("tone date=$date", full.tone, cyclic.tone)
            assertEquals("fantoms date=$date", full.fantoms, cyclic.fantoms)
            date = date.plusDays(1)
        }
        // 260 days apart = same point in the cycle = same cached object
        val a = MayaCalendar.cycleInfo(LocalDate.of(2024, 1, 1))
        val b = MayaCalendar.cycleInfo(LocalDate.of(2024, 1, 1).plusDays(260))
        assertEquals(true, a === b)
    }
}
