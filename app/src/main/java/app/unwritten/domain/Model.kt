package app.unwritten.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

/** What sort of book it is. The first five stand on the fiction shelf, the rest in the reference room. */
enum class Kind(val fiction: Boolean) {
    NOVEL(true), NOVELLA(true), STORIES(true), POEMS(true), TALES(true),
    PHRASEBOOK(false), FIELD_GUIDE(false), MANUAL(false), DREAMBOOK(false), GUIDEBOOK(false), COOKBOOK(false),
    ENCYCLOPEDIA(false), INDEX(false), TIMETABLE(false), LETTERS(false), TEXTBOOK(false), ATLAS(false);

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name == s } ?: NOVEL
    }
}

/** How the cover looks: a Soviet-style series in a frame, a three-band paperback, pulp, or typed samizdat. */
enum class Style { SERIES, TRICOLOUR, PULP, SAMIZDAT;

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name == s } ?: SERIES
    }
}

/** A customer's special wish: the whole book in six words, only its contents, no «о» (no «e»), or only how it ends. */
enum class Rule { NONE, SIX, TOC, LIPO, ENDING;

    companion object {
        fun of(s: String?) = when (s?.trim()?.lowercase()) {
            "six" -> SIX; "toc" -> TOC; "lipo" -> LIPO; "ending" -> ENDING; else -> NONE
        }
    }
}

/** A book that doesn't exist: only what surrounds its text. */
@Entity(tableName = "book")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val created: Long,
    val kind: Kind,
    val style: Style,
    val title: String,
    val author: String = "",
    val epigraph: String = "",
    val first: String = "",
    /** One chapter per line. */
    val toc: String = "",
    val middle: String = "",
    val last: String = "",
    val blurb: String = "",
    val pages: Int = 240,
    /** The customer's note it was found for, as it read then; empty when written for nobody. */
    val note: String = "",
    val requestId: String = "",
    val visitor: Int = -1,
    val rule: Rule = Rule.NONE,
    /** «См. также»: another book of the shop. */
    val refId: Long = 0,
    /** A pencil note in the margin, left as a reader. */
    val margin: String = "",
    /** In the box by the door, ten kopecks each. */
    val bargain: Boolean = false,
    val edition: Int = 1,
    /** The preface to the latest edition. */
    val preface: String = "",
    /** Days (epoch days) the book was opened: the stamps on its library card. */
    val opens: String = "",
) {
    val chapters: List<String> get() = toc.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val openDays: List<Long> get() = opens.split(',').mapNotNull { it.toLongOrNull() }
    fun day(zone: ZoneId): Long = created.day(zone)
}

/** What a critic wrote about a book. */
@Entity(tableName = "review")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val at: Long,
    val critic: Int,
    val template: Int,
    val stars: Int,
)

fun Long.day(zone: ZoneId): Long = Instant.ofEpochMilli(this).atZone(zone).toLocalDate().toEpochDay()

/** The whole shop at one moment. */
class Shop(val books: List<Book>, val reviews: List<Review>) {
    val shelved = books.filter { !it.bargain }
    val fiction = shelved.filter { it.kind.fiction }
    val reference = shelved.filter { !it.kind.fiction }
    val bin = books.filter { it.bargain }
    val pages get() = books.sumOf { it.pages }
    val served: Set<String> = books.mapNotNull { it.requestId.ifEmpty { null } }.toSet()

    fun book(id: Long) = books.firstOrNull { it.id == id }
    fun reviews(id: Long) = reviews.filter { it.bookId == id }.sortedByDescending { it.at }

    /** Books that point here with «see also». */
    fun citedBy(id: Long) = books.filter { it.refId == id }

    /**
     * A book that comes back from a reader: written a fortnight ago or more, not opened for a week, nothing in its
     * margins yet. One a day at most, and only some days, so it stays a small surprise.
     */
    fun returned(today: Long, zone: ZoneId): Book? {
        if (Math.floorMod(today, 3L) != 0L) return null
        return shelved.filter { b ->
            b.margin.isBlank() && today - b.day(zone) >= Rules.RETURN_AFTER_DAYS && today - (b.openDays.maxOrNull() ?: b.day(zone)) >= Rules.RETURN_QUIET_DAYS
        }.minByOrNull { it.openDays.maxOrNull() ?: it.day(zone) }
    }
}

object Rules {
    const val RINGS_A_DAY = 3
    const val REVIEWS_PER_BOOK = 5
    const val RETURN_AFTER_DAYS = 14
    const val RETURN_QUIET_DAYS = 7
    /** A new edition only once the first has stood on the shelf a week. */
    const val EDITION_AFTER_DAYS = 7
    const val SIX_WORDS = 6
    const val MIN_PAGES = 8
    const val MAX_PAGES = 1200

    fun words(s: String) = s.split(Regex("\\s+")).count { w -> w.any { it.isLetterOrDigit() } }

    /** The letter a lipogram may not use. */
    fun lipoLetter(ru: Boolean) = if (ru) 'о' else 'e'
    fun lipoCount(s: String, ru: Boolean) = s.count { it.lowercaseChar() == lipoLetter(ru) }
}

/** A deterministic hash for looks that must not change between launches: spine heights, series names, years. */
fun hash(s: String): Int {
    var h = 7
    for (c in s) h = h * 31 + c.code
    return h and 0x7fffffff
}
