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
