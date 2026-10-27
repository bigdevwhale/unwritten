package app.unwritten

import app.unwritten.domain.Book
import app.unwritten.domain.Kind
import app.unwritten.domain.Moon
import app.unwritten.domain.Phase
import app.unwritten.domain.Place
import app.unwritten.domain.Requests
import app.unwritten.domain.Rule
import app.unwritten.domain.Rules
import app.unwritten.domain.Shop
import app.unwritten.domain.Sky
import app.unwritten.domain.Style
import app.unwritten.domain.Weather
import app.unwritten.domain.WeatherNow
import app.unwritten.ui.Words
import app.unwritten.ui.write.missing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class LogicTest {
    private val zone = ZoneId.of("Europe/Prague")
    private val catalogue by lazy { Requests.load { File("src/main/assets/requests.tsv").takeIf { it.exists() }?.inputStream() } }

    @Test fun `the catalogue loads with every kind`() {
        assertTrue(catalogue.all.size >= 400)
        assertEquals(Kind.entries.toSet(), catalogue.all.map { it.kind }.toSet())
        assertTrue(catalogue.all.any { it.rule == Rule.SIX })
        assertTrue(catalogue.all.all { it.ru.isNotBlank() && it.en.isNotBlank() })
    }

    @Test fun `the same customer stands at the counter all day, and the bell brings another`() {
        val a = catalogue.visit(20_000, 0, emptySet(), 22)!!
        assertEquals(a, catalogue.visit(20_000, 0, emptySet(), 22))
        assertNotEquals(a.request, catalogue.visit(20_000, 1, emptySet(), 22)!!.request)
        assertNotEquals(a.request, catalogue.visit(20_001, 0, emptySet(), 22)!!.request)
    }

    @Test fun `a served note does not come back`() {
        val a = catalogue.visit(20_000, 0, emptySet(), 22)!!
        assertNotEquals(a.request.id, catalogue.visit(20_000, 0, setOf(a.request.id), 22)!!.request.id)
    }

    @Test fun `a book needs a title, a first line and a blurb, bent by the customer's wish`() {
        assertNotNull(missing(Rule.NONE, "", "a", "b", "", "", true))
        assertNotNull(missing(Rule.NONE, "T", "", "b", "", "", true))
        assertNull(missing(Rule.NONE, "T", "a", "b", "", "", true))
        assertNotNull(missing(Rule.SIX, "T", "one two three four five six seven", "b", "", "", false))
        assertNull(missing(Rule.SIX, "T", "Forgotten, waited, found, opened. Rain.", "b", "", "", false))
        assertNotNull(missing(Rule.TOC, "T", "", "b", "one\ntwo", "", true))
        assertNull(missing(Rule.TOC, "T", "", "b", "one\ntwo\nthree", "", true))
        assertNotNull(missing(Rule.ENDING, "T", "", "b", "", "", true))
        assertNull(missing(Rule.ENDING, "T", "", "b", "", "Конец.", true))
        assertNotNull(missing(Rule.LIPO, "Сад", "Тихий вечер в саду", "Книга о саде", "", "", true))
        assertNull(missing(Rule.LIPO, "Сад", "Тихий вечер в саду", "Книга сна", "", "", true))
        assertEquals(6, Rules.words("Forgotten, waited, found — opened. Rain fell."))
    }

    private fun book(id: Long, daysAgo: Long, today: Long, margin: String = "", opened: Long = daysAgo) = Book(
        id = id, created = LocalDate.ofEpochDay(today - daysAgo).atTime(12, 0).atZone(zone).toInstant().toEpochMilli(),
        kind = Kind.NOVEL, style = Style.SERIES, title = "B$id", margin = margin, opens = "${today - opened}",
    )

    @Test fun `a quiet book with empty margins comes back from a reader some days`() {
        val today = 20_001L // divisible by 3
        val shop = Shop(listOf(book(1, 20, today, opened = 10), book(2, 20, today, margin = "!", opened = 10), book(3, 3, today)), emptyList())
        assertEquals(1L, shop.returned(today, zone)?.id)
        assertNull(shop.returned(today + 1, zone))
        assertNull(Shop(listOf(book(1, 20, today, opened = 2)), emptyList()).returned(today, zone))
    }

    @Test fun `the window follows the real time of day`() {
        val prague = Place("Praha", "", 50.08, 14.42)
        fun at(h: Int) = Sky.now(ZonedDateTime.of(2026, 10, 27, h, 0, 0, 0, zone), prague, null)
        assertEquals(Phase.NIGHT, at(2).phase)
        assertEquals(Phase.DAY, at(13).phase)
        assertEquals(Phase.NIGHT, at(22).phase)
        assertNotNull(at(13).sun)
        assertNull(at(13).moon)
        assertNotNull(at(23).moon)
        assertTrue(at(9).sun!! < at(15).sun!!)
    }

    @Test fun `fresh real weather wins, stale weather gives way to the shop's own`() {
        val now = ZonedDateTime.of(2026, 10, 27, 13, 0, 0, 0, zone)
        val ms = now.toInstant().toEpochMilli()
        assertEquals(Weather.SNOW, Sky.now(now, null, WeatherNow(Weather.SNOW, -3.0, ms - 60_000)).weather)
        assertEquals(-3.0, Sky.now(now, null, WeatherNow(Weather.SNOW, -3.0, ms - 60_000)).temp!!, 0.0)
        assertNull(Sky.now(now, null, WeatherNow(Weather.SNOW, -3.0, ms - Sky.FRESH_MS - 1)).temp)
    }

    @Test fun `weather codes and the moon`() {
        assertEquals(Weather.RAIN, Weather.wmo(63))
        assertEquals(Weather.STORM, Sky.symbol("heavyrainandthunder"))
        val full = ZonedDateTime.of(2026, 10, 26, 6, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        assertTrue(kotlin.math.abs(Moon.phase(full) - .5) < .05)
    }

    @Test fun `critics have something to say in both languages`() {
        for (ru in listOf(true, false)) {
            val w = Words(ru)
            for (i in 0 until w.reviewCount) {
                val s = w.review(i, "Зонт", "И. З.", Kind.MANUAL)
                assertTrue(s, '{' !in s)
            }
            assertTrue(w.edition(2).isNotBlank())
        }
    }
}
