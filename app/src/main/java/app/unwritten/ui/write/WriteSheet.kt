package app.unwritten.ui.write

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.container
import app.unwritten.domain.Book
import app.unwritten.domain.Kind
import app.unwritten.domain.Rule
import app.unwritten.domain.Rules
import app.unwritten.domain.Style
import app.unwritten.domain.Visit
import app.unwritten.ui.Home
import app.unwritten.ui.Nav
import app.unwritten.ui.art.Cover
import app.unwritten.ui.art.Face
import app.unwritten.ui.components.Btn
import app.unwritten.ui.components.Chip
import app.unwritten.ui.components.Field
import app.unwritten.ui.components.Flow
import app.unwritten.ui.components.Gap
import app.unwritten.ui.components.Label
import app.unwritten.ui.components.LocalToast
import app.unwritten.ui.components.Sheet
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import kotlin.math.roundToInt

/** What has to be on the card before the book can go up, under the customer's wish. */
fun missing(rule: Rule, title: String, first: String, blurb: String, toc: String, last: String, ru: Boolean): String? = when {
    title.isBlank() -> if (ru) "Нужно название." else "It needs a title."
    blurb.isBlank() -> if (ru) "Нужна аннотация для оборота." else "It needs a blurb for the back."
    rule == Rule.TOC && toc.lines().count { it.isNotBlank() } < 3 -> if (ru) "Покупателю нужно оглавление — хотя бы три главы." else "The customer wants the contents: three chapters at least."
    rule == Rule.ENDING && last.isBlank() -> if (ru) "Покупателю нужна последняя фраза." else "The customer wants the last line."
    rule != Rule.TOC && rule != Rule.ENDING && first.isBlank() -> if (ru) "Нужна первая фраза." else "It needs a first line."
    rule == Rule.SIX && Rules.words(first) > Rules.SIX_WORDS -> if (ru) "Первая фраза длиннее шести слов." else "The first line runs past six words."
    rule == Rule.LIPO && Rules.lipoCount(title + first + blurb, ru) > 0 -> if (ru) "Где-то прячется «о»: ${Rules.lipoCount(title + first + blurb, ru)}." else "An “e” is hiding somewhere: ${Rules.lipoCount(title + first + blurb, ru)}."
    else -> null
}

/** The card of a book being found: three lines are enough, the rest only if wanted. */
@Composable
fun WriteSheet(h: Home, nav: Nav, visit: Visit?) {
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val w = h.w
    val rule = visit?.request?.rule ?: Rule.NONE
    var kind by rememberSaveable { mutableStateOf(visit?.request?.kind ?: Kind.NOVEL) }
    var style by rememberSaveable { mutableStateOf(Style.SERIES) }
    var title by rememberSaveable { mutableStateOf("") }
    var first by rememberSaveable { mutableStateOf("") }
    var blurb by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var epigraph by rememberSaveable { mutableStateOf("") }
    var toc by rememberSaveable { mutableStateOf("") }
    var middle by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var ref by rememberSaveable { mutableLongStateOf(0L) }
    var pages by rememberSaveable { mutableFloatStateOf(240f) }
    var more by rememberSaveable { mutableStateOf(rule == Rule.TOC || rule == Rule.ENDING) }

    Sheet(if (visit != null) w.t("Ищем на полке…", "Looking on the shelf…") else w.t("Книга для себя", "A book for nobody"), { nav.pop() }, w.t("Отложить", "Put away")) {
        if (visit != null) {
            Text("«${visit.request.text(h.ru)}»", style = T.Note.copy(color = C.accent, fontSize = 22.sp, lineHeight = 24.sp))
            if (rule != Rule.NONE) Text(w.rule(rule), style = T.Small.copy(color = C.accent))
        } else {
            Text(w.t("Без заказа: книга, которую никто не просил, кроме тебя.", "No order: a book nobody asked for, except you."), style = T.Small)
        }

        Label(w.t("Что это за книга", "What kind of book"))
        Flow { for (k in Kind.entries) Chip(w.kind(k), k == kind) { kind = k } }

        Label(w.t("Название", "Title"))
        Field(title, { title = it }, w.t("Зонт, который остался", "The Umbrella That Stayed"), style = T.Heading, max = 90, single = true)

        if (rule != Rule.TOC && rule != Rule.ENDING || first.isNotEmpty()) {
            Label(if (rule == Rule.SIX) w.t("Первая фраза — вся книга, до шести слов (${Rules.words(first)})", "The first line is the whole book, six words at most (${Rules.words(first)})") else w.t("Первая фраза", "First line"))
            Field(first, { first = it }, w.t("В то утро его забыли в третий раз, и он решил, что это знак.", "That morning it was forgotten for the third time, and it took that as a sign."), minLines = 2, max = 400)
        }
        if (rule == Rule.ENDING) {
            Label(w.t("Последняя фраза — то, ради чего пришёл покупатель", "The last line: what the customer came for"))
            Field(last, { last = it }, w.t("Дождь начался, как только его раскрыли.", "The rain began the moment it was opened."), minLines = 2, max = 400)
        }
        if (rule == Rule.TOC) {
            Label(w.t("Оглавление — по главе на строку", "Contents, a chapter per line"))
            Field(toc, { toc = it }, w.t("Гардероб\nДождь в четверг\nБюро находок", "The Cloakroom\nRain on Thursday\nLost Property"), minLines = 5, max = 1200)
        }

        Label(w.t("Аннотация — на обороте", "Blurb, for the back"))
        Field(blurb, { blurb = it }, w.t("Трогательная и смешная история о вещи, которая ждала…", "A funny, tender story about a thing that waited…"), minLines = 3, max = 700)

        Label(w.t("Обложка", "Cover"))
        val face = Face(title.ifBlank { w.t("Название", "Title") }, author, kind, style)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (s in Style.entries) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.clip(RoundedCornerShape(4.dp)).then(if (s == style) Modifier.border(2.dp, C.accent, RoundedCornerShape(4.dp)) else Modifier)
                            .clickable { style = s }.padding(3.dp),
                    ) { Cover(face.copy(style = s), w, h.imprint, Modifier.width(62.dp)) }
                    Text(w.style(s), style = T.Small.copy(fontSize = 11.sp))
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Cover(face, w, h.imprint, Modifier.width(170.dp))
        }

        Text(
            (if (more) "▾ " else "▸ ") + w.t("Ещё про книгу — по желанию", "More about the book, if you like"),
            style = T.BodyB.copy(color = C.accent), modifier = Modifier.clickable { more = !more }.padding(vertical = 8.dp),
        )
        if (more) {
            Label(w.t("Автор — псевдоним, конечно", "Author: a pen name, of course"))
            Field(author, { author = it }, w.t("И. Забытов", "I. M. Forgotten"), max = 60, single = true)
            Label(w.t("Эпиграф", "Epigraph"))
            Field(epigraph, { epigraph = it }, w.t("«Все вещи помнят руки». — Из разговора в гардеробе", "‘All things remember hands.’ — Overheard in a cloakroom"), minLines = 2, max = 300)
            if (rule != Rule.TOC) {
                Label(w.t("Оглавление — по главе на строку", "Contents, a chapter per line"))
                Field(toc, { toc = it }, w.t("Гардероб\nДождь в четверг\nБюро находок", "The Cloakroom\nRain on Thursday\nLost Property"), minLines = 4, max = 1200)
            }
            if (rule == Rule.TOC || rule == Rule.ENDING) {
                Label(w.t("Первая фраза", "First line"))
                Field(first, { first = it }, w.t("С чего всё начинается…", "Where it all begins…"), minLines = 2, max = 400)
            }
            Label(w.t("Страница из середины", "A page from the middle"))
            Field(middle, { middle = it }, w.t("Он висел на крючке под номером сорок семь…", "It hung on peg forty-seven…"), minLines = 5, max = 2000)
            if (rule != Rule.ENDING) {
                Label(w.t("Последняя фраза", "Last line"))
                Field(last, { last = it }, w.t("И всё стало, как было, только лучше.", "And everything was as it had been, only better."), minLines = 2, max = 400)
            }
            if (h.shop.books.isNotEmpty()) {
                Label(w.t("См. также — другая книга лавки", "See also: another book of the shop"))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("—", ref == 0L) { ref = 0 }
                    for (b in h.shop.books.reversed()) Chip(b.title, ref == b.id) { ref = b.id }
                }
            }
            Label(w.t("Сколько в ней страниц: ${pages.roundToInt()}", "How many pages: ${pages.roundToInt()}"))
            Slider(
                pages, { pages = it }, valueRange = Rules.MIN_PAGES.toFloat()..900f,
                colors = SliderDefaults.colors(thumbColor = C.accent, activeTrackColor = C.accent, inactiveTrackColor = C.line),
            )
        }

        val why = missing(rule, title, first, blurb, toc, last, h.ru)
        Gap(14)
        if (why != null && (title.isNotEmpty() || blurb.isNotEmpty())) Text(why, style = T.Small.copy(color = C.accent))
        Btn(w.t("Поставить на полку", "Put it on the shelf"), Modifier.fillMaxWidth().padding(top = 8.dp), enabled = why == null) {
            val b = Book(
                created = 0, kind = kind, style = style, title = title.trim(), author = author.trim(), epigraph = epigraph.trim(), first = first.trim(),
                toc = toc.trim(), middle = middle.trim(), last = last.trim(), blurb = blurb.trim(), pages = pages.roundToInt(),
                note = visit?.request?.text(h.ru) ?: "", requestId = visit?.request?.id ?: "", visitor = visit?.visitor ?: -1, rule = rule, refId = ref,
            )
            c.write {
                val id = repo.shelve(b)
                nav.fresh = id
            }
            nav.pop()
            toast.show(if (visit != null) w.t("Нашлась! Покупатель ушёл довольный.", "Found it! The customer left happy.") else w.t("Книга встала на полку.", "The book is on the shelf."))
        }
        Gap(24)
    }
}
