package app.unwritten.domain

import java.io.InputStream
import kotlin.random.Random

/** A customer's note: what they're looking for. */
data class Request(val id: String, val kind: Kind, val rule: Rule, val ru: String, val en: String) {
    fun text(ru: Boolean) = if (ru) this.ru else en
}

/** Who stands at the counter today, and with which note. */
data class Visit(val request: Request, val visitor: Int)

/** The catalogue of notes from assets/requests.tsv: id, kind, rule, Russian, English. */
class Requests(val all: List<Request>) {
    private val byId = all.associateBy { it.id }
    fun byId(id: String) = byId[id]

    /**
     * Today's note: the same all day, chosen among those nobody has been served yet. [ring] moves to the next
     * customer when the bell rings; the visitor changes with the note.
     */
    fun visit(day: Long, ring: Int, served: Set<String>, visitors: Int): Visit? {
        val open = all.filter { it.id !in served }.ifEmpty { all }
        if (open.isEmpty()) return null
        val r = Random(day * 131 + 17)
        val order = open.shuffled(r)
        val req = order[ring % order.size]
        return Visit(req, Math.floorMod(hash(req.id) + day.toInt(), visitors))
    }

    companion object {
        fun parse(lines: Sequence<String>): Requests = Requests(
            lines.mapNotNull { line ->
                val p = line.split('\t')
                if (p.size < 5 || p[0].isBlank()) null
                else Request(p[0].trim(), Kind.of(p[1].trim()), Rule.of(p[2]), p[3].trim(), p[4].trim())
            }.toList(),
        )

        fun load(open: () -> InputStream?): Requests =
            open()?.bufferedReader(Charsets.UTF_8)?.use { parse(it.lineSequence()) } ?: Requests(emptyList())
    }
}
