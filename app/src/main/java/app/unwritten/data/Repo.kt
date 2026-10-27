package app.unwritten.data

import app.unwritten.domain.Book
import app.unwritten.domain.Review
import app.unwritten.domain.Rules
import app.unwritten.domain.Shop
import app.unwritten.domain.day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId

/** The shop's books and what critics said of them, in Room. */
class Repo(db: ShopDb) {
    private val dao = db.dao()
    private val mutex = Mutex()
    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    /** The clock; the demo seed winds it back to replay weeks. */
    var clock: () -> Long = System::currentTimeMillis
    var zone: ZoneId = ZoneId.systemDefault()

    /** While true, changes aren't shown, so the demo seed doesn't flicker through its weeks. */
    @Volatile var hold = false

    suspend fun refresh(): Shop = Shop(dao.books(), dao.reviews()).also { if (!hold) _shop.value = it }

    private suspend fun <T> op(block: suspend (Shop, Long) -> T): T = mutex.withLock {
        val s = refresh()
        block(s, clock()).also { refresh() }
    }

    /** A book is found on the shelf: it goes up with its first stamp. */
    suspend fun shelve(b: Book): Long = op { _, now ->
        dao.insert(b.copy(id = 0, created = now, opens = now.day(zone).toString(), pages = b.pages.coerceIn(Rules.MIN_PAGES, Rules.MAX_PAGES)))
    }

    /** Opening a book stamps its card, once a day. */
    suspend fun open(id: Long) = op { s, now ->
        val b = s.book(id) ?: return@op
        val d = now.day(zone)
        if (d !in b.openDays) dao.update(b.copy(opens = (b.openDays + d).joinToString(",")))
    }

    suspend fun margin(id: Long, text: String) = op { s, _ -> s.book(id)?.let { dao.update(it.copy(margin = text.trim().take(400))) } }

    suspend fun bargain(id: Long, on: Boolean) = op { s, _ -> s.book(id)?.let { dao.update(it.copy(bargain = on)) } }

    /** A new edition: a preface, maybe one more chapter. */
    suspend fun reissue(id: Long, preface: String, chapter: String) = op { s, _ ->
        val b = s.book(id) ?: return@op
        val toc = if (chapter.isBlank()) b.toc else (b.chapters + chapter.trim()).joinToString("\n")
        dao.update(b.copy(edition = b.edition + 1, preface = preface.trim(), toc = toc))
    }

    /**
     * A critic calls: one review a day per book, a handful at most. [pick] chooses the critic and a template not
     * used on this book yet; null when there's nothing new to say.
     */
    suspend fun review(id: Long, pick: (used: Set<Int>, seed: Long) -> Pair<Int, Int>?): Review? = op { s, now ->
        val old = s.reviews(id)
        if (old.size >= Rules.REVIEWS_PER_BOOK || old.any { it.at.day(zone) == now.day(zone) }) return@op null
        val (critic, template) = pick(old.map { it.template }.toSet(), now / 1000 + id * 7) ?: return@op null
        val r = Review(bookId = id, at = now, critic = critic, template = template, stars = 1 + Math.floorMod(now / 1000 + id * 13 + template, 5L).toInt())
        r.copy(id = dao.insert(r))
    }
}
