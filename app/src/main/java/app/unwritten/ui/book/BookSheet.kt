package app.unwritten.ui.book

import androidx.compose.foundation.Canvas
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.container
import app.unwritten.domain.Book
import app.unwritten.domain.Rules
import app.unwritten.domain.day
import app.unwritten.domain.hash
import app.unwritten.ui.Home
import app.unwritten.ui.Nav
import app.unwritten.ui.Overlay
import app.unwritten.ui.Page
import app.unwritten.ui.Words
import app.unwritten.ui.art.Cover
import app.unwritten.ui.art.Face
import app.unwritten.ui.components.Btn
import app.unwritten.ui.components.Field
import app.unwritten.ui.components.Flow
import app.unwritten.ui.components.Gap
import app.unwritten.ui.components.Kind
import app.unwritten.ui.components.Label
import app.unwritten.ui.components.LocalToast
import app.unwritten.ui.components.Sheet
import app.unwritten.ui.components.paper
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import app.unwritten.ui.theme.Typewriter
import app.unwritten.zine.Zine
import java.time.LocalDate

@Composable
fun BookSheet(h: Home, nav: Nav, id: Long, start: Page) {
    val ctx = LocalContext.current
    val c = ctx.container
    val toast = LocalToast.current
    val w = h.w
    val b = h.shop.book(id) ?: run { nav.pop(); return }
    LaunchedEffect(id) { c.repo.open(id) }
    var page by rememberSaveable(id) { mutableStateOf(start) }
    val reviews = h.shop.reviews(b.id)

    Sheet(w.kind(b.kind) + " · " + w.pages(b.pages), { nav.pop() }, w.t("На полку", "Back"), scroll = false) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            val tabs = listOf(
                Page.COVER to w.t("Обложка", "Cover"), Page.BACK to w.t("Оборот", "Back"), Page.OPEN to w.t("Внутри", "Inside"),
                Page.CARD to w.t("Формуляр", "Card"), Page.REVIEWS to w.t("Критика", "Critics") + if (reviews.isNotEmpty()) " ${reviews.size}" else "",
            )
            for ((p, label) in tabs) {
                val on = p == page
                Text(
                    label, maxLines = 1, style = T.Italic.copy(color = if (on) C.ink else C.muted, fontSize = 12.5.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f).clickable { page = p }.drawBehind { if (on) drawRect(C.accent, Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx())) }.padding(vertical = 6.dp),
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when (page) {
                Page.COVER -> CoverPage(h, nav, b)
                Page.BACK -> BackPage(h, b)
                Page.OPEN -> OpenPages(h, b)
                Page.CARD -> CardPage(h, b)
                Page.REVIEWS -> Reviews(h, b)
            }
        }

        // The actions stay at the foot of the sheet, two by two.
        Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn(w.t("Позвать критика", "Call a critic"), Modifier.weight(1f)) {
                    page = Page.REVIEWS
                    c.write {
                        val r = repo.review(b.id) { used, seed ->
                            val free = (0 until w.reviewCount).filter { it !in used }
                            if (free.isEmpty()) null else Math.floorMod(seed / 3, w.critics.size.toLong()).toInt() to free[Math.floorMod(seed, free.size.toLong()).toInt()]
                        }
                        if (r == null) toast.show(w.t("Критики на сегодня всё сказали. Завтра придут ещё.", "The critics have said their piece today. More tomorrow."))
                    }
                }
                Btn(w.t("Напечатать зин", "Print a zine"), Modifier.weight(1f), kind = Kind.GHOST) {
                    c.write { Zine.share(ctx, b, w, h.imprint) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (h.today - b.day(h.zone) >= Rules.EDITION_AFTER_DAYS && !b.bargain) {
                    Btn(w.t("Новое издание", "New edition"), Modifier.weight(1f), kind = Kind.GHOST) { nav.push(Overlay.Reissue(b.id)) }
                }
                Btn(if (b.bargain) w.t("Вернуть на полку", "Back on the shelf") else w.t("В уценку", "To the bargain box"), Modifier.weight(1f), kind = Kind.GHOST) {
                    c.write { repo.bargain(b.id, !b.bargain) }
                    nav.pop()
                    toast.show(if (b.bargain) w.t("Снова на полке.", "Back on the shelf.") else w.t("Ушла в уценку. Кто-нибудь найдёт.", "Into the bargain box. Someone will find it."))
                }
            }
        }
    }
}

/** A cover as large as the room left for it allows. */
@Composable
private fun ColumnScope.BigCover(h: Home, b: Book, share: Float) {
    BoxWithConstraints(Modifier.fillMaxWidth().weight(share), contentAlignment = Alignment.Center) {
        val width = minOf(maxWidth * .7f, maxHeight * (2f / 3f) - 4.dp)
        Box(Modifier.width(width).shadow(12.dp)) { Cover(Face(b), h.w, h.imprint, Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun ColumnScope.CoverPage(h: Home, nav: Nav, b: Book) {
    val w = h.w
    BigCover(h, b, 1f)
    if (b.note.isNotEmpty()) {
        Text("«${b.note}»", style = T.Note.copy(fontSize = 19.sp, lineHeight = 21.sp), maxLines = 3, modifier = Modifier.padding(top = 10.dp))
        if (b.visitor in w.visitors.indices) Text("— ${w.visitors[b.visitor]}" + (if (b.edition > 1) " · ${w.edition(b.edition).lowercase()}" else ""), style = T.Small.copy(fontSize = 12.5.sp))
    } else if (b.edition > 1) Text(w.edition(b.edition), style = T.Small, modifier = Modifier.padding(top = 8.dp))
    val links = listOfNotNull(h.shop.book(b.refId)) + h.shop.citedBy(b.id)
    if (links.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            Text(w.t("см. также: ", "see also: "), style = T.Label)
            for (o in links) Text("«${o.title}»  ", style = T.Italic.copy(color = C.accent, fontSize = 15.sp), maxLines = 1,
                modifier = Modifier.clickable { nav.pop(); nav.push(Overlay.Book(o.id)) })
        }
    }
}

@Composable
private fun ColumnScope.BackPage(h: Home, b: Book) {
    val w = h.w
    val f = Face(b)
    val tint = Color.hsl(f.hue, .25f, .88f)
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        val width = minOf(maxWidth * .82f, maxHeight * (2f / 3f) - 4.dp)
        Column(Modifier.width(width).aspectRatio(2f / 3f).shadow(12.dp).background(tint).padding(18.dp)) {
            BasicText(
                b.blurb, style = T.Body.copy(fontSize = 14.sp, lineHeight = 20.sp), modifier = Modifier.weight(1f, fill = false),
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp, stepSize = .5.sp),
            )
            Text(
                "${b.author.ifBlank { w.t("Автор неизвестен", "Author unknown") }}. ${b.title}. — ${w.t("Изд-во", "")} «${w.imprint(h.imprint)}», ${1951 + f.seed % 40}. — ${b.pages} ${w.t("с.", "pp.")}",
                style = T.Small.copy(fontSize = 10.sp, lineHeight = 13.sp), maxLines = 3, modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.weight(.001f))
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(70.dp, 24.dp).background(Color.White).padding(3.dp)) {
                        val code = hash(b.title + b.id)
                        var x = 0f; var i = 0
                        while (x < size.width) {
                            val bw = (1 + (code shr (i % 28)) % 3) * .9f * density
                            if (i % 2 == 0) drawRect(Color(0xFF222222), Offset(x, 0f), Size(bw, size.height))
                            x += bw; i++
                        }
                    }
                    Text("ISBN 0-000-${hash(b.title).toString().take(5)}-0", style = T.Small.copy(fontSize = 7.sp, lineHeight = 9.sp, color = C.ink))
                }
            }
        }
    }
}

/** A page of the book itself: cream, a page number at the foot. Fills the room; a long page scrolls inside itself. */
@Composable
private fun BookPage(num: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().shadow(4.dp).paper(C.paperLight).drawBehind {
            drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .07f), Color.Transparent), 0f, 20.dp.toPx()))
        }.padding(start = 24.dp, end = 22.dp, top = 22.dp, bottom = 10.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, content = content)
        if (num != null) Text(num, style = T.Small.copy(fontSize = 11.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

/** The inside of the book, one page at a time: swipe or tap the arrows to turn. */
@Composable
private fun ColumnScope.OpenPages(h: Home, b: Book) {
    val c = LocalContext.current.container
    val w = h.w
    val pages = buildList<Pair<String?, @Composable ColumnScope.() -> Unit>> {
        if (b.edition > 1 && b.preface.isNotBlank()) add("v" to {
            Text(w.t("Предисловие к изданию", "Preface to the edition").uppercase(), style = T.Small.copy(fontSize = 10.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
            Text(b.preface, style = T.Body)
        })
        if (b.first.isNotBlank() || b.epigraph.isNotBlank()) add("7" to {
            if (b.epigraph.isNotBlank()) Text(b.epigraph, style = T.Italic.copy(fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.End), modifier = Modifier.padding(start = 44.dp, bottom = 22.dp).fillMaxWidth())
            if (b.first.isNotBlank()) Text(buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 44.sp, color = C.accent)) { append(b.first.take(1)) }
                append(b.first.drop(1))
            }, style = T.Body.copy(fontSize = 17.sp, lineHeight = 26.sp))
        })
        if (b.chapters.isNotEmpty()) add(null to {
            Text(w.t("Содержание", "Contents").uppercase(), style = T.Small.copy(fontSize = 10.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
            val n = b.chapters.size
            b.chapters.forEachIndexed { i, ch ->
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(ch, style = T.Body.copy(fontSize = 15.sp), modifier = Modifier.widthIn(max = 230.dp))
                    Box(Modifier.weight(1f).height(1.dp).padding(horizontal = 4.dp).drawBehind {
                        var x = 0f
                        while (x < size.width) { drawCircle(C.muted, .8.dp.toPx(), Offset(x, 0f)); x += 4.dp.toPx() }
                    })
                    Text("${7 + i * b.pages / n}", style = T.Body.copy(fontSize = 15.sp))
                }
            }
        })
        if (b.middle.isNotBlank()) add("${b.pages / 2}" to {
            Text(b.middle, style = T.Body.copy(fontSize = 15.sp, lineHeight = 23.sp))
            if (b.margin.isNotBlank()) Text(b.margin, style = T.Pencil, modifier = Modifier.padding(top = 12.dp).rotate(-1f))
        })
        if (b.last.isNotBlank()) add("${b.pages}" to {
            Text(b.last, style = T.Italic.copy(textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
            Text("❦", style = T.Heading.copy(color = C.accent, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        })
        // The flyleaf, where a reader leaves pencil.
        add(null to {
            var note by remember(b.id) { mutableStateOf(b.margin) }
            Text(w.t("Пометка на полях — карандашом, как читатель", "A note in the margin, in pencil, as a reader"), style = T.Label)
            Field(note, { note = it }, w.t("вот тут я заплакал(а), честно", "this is where I cried, honestly"), style = T.Pencil, minLines = 3, max = 400)
            if (note.trim() != b.margin) Btn(w.t("Оставить пометку", "Leave the note"), Modifier.padding(top = 6.dp), Kind.GHOST) { c.write { repo.margin(b.id, note) } }
        })
    }
    val state = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    HorizontalPager(state, Modifier.fillMaxWidth().weight(1f), pageSpacing = 14.dp) { i ->
        BookPage(pages[i].first, content = pages[i].second)
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", style = T.Heading.copy(color = if (state.currentPage > 0) C.accent else C.line), modifier = Modifier.clickable { scope.launch { state.animateScrollToPage(state.currentPage - 1) } }.padding(horizontal = 18.dp))
        Text("${state.currentPage + 1} / ${pages.size}", style = T.Small.copy(textAlign = TextAlign.Center), modifier = Modifier.weight(1f))
        Text("›", style = T.Heading.copy(color = if (state.currentPage < pages.lastIndex) C.accent else C.line), modifier = Modifier.clickable { scope.launch { state.animateScrollToPage(state.currentPage + 1) } }.padding(horizontal = 18.dp))
    }
}

@Composable
private fun ColumnScope.Reviews(h: Home, b: Book) {
    val w = h.w
    val reviews = h.shop.reviews(b.id)
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
        if (reviews.isEmpty()) Text(w.t("Критики ещё не заходили. Позови — придут.", "No critic has called yet. Call one."), style = T.Small, modifier = Modifier.padding(top = 20.dp))
        for (r in reviews) {
            Column(Modifier.padding(bottom = 10.dp).fillMaxWidth().shadow(3.dp).background(C.paperLight).padding(14.dp)) {
                Text(buildAnnotatedString {
                    append(w.critics[r.critic % w.critics.size])
                    append("  ")
                    withStyle(SpanStyle(color = C.accent)) { append("★".repeat(r.stars) + "☆".repeat(5 - r.stars)) }
                }, style = T.Small.copy(fontSize = 12.sp))
                Text(w.review(r.template, b.title, b.author, b.kind), style = T.Body.copy(fontSize = 15.sp, lineHeight = 21.sp), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.CardPage(h: Home, b: Book) {
    val w = h.w
    Column(Modifier.fillMaxWidth().shadow(6.dp).background(Color(0xFFF1E6C9)).padding(16.dp)) {
        Text(w.t("КНИЖНЫЙ ФОРМУЛЯР", "LIBRARY CARD"), style = T.Card.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(2.dp).background(Color(0xFFC2A97A)))
        Text(b.author.ifBlank { "—" }, style = T.Card)
        Text(b.title, style = T.Card)
        Text(w.t("Шифр", "Shelfmark") + " ${shelfmark(b)}", style = T.Card.copy(color = C.muted, fontSize = 11.sp), modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
        Flow(Modifier.fillMaxWidth().drawBehind {
            var y = 28.dp.toPx()
            while (y < size.height) { drawLine(Color(0xFFC9B28A), Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += 28.dp.toPx() }
        }.height(maxOf(180, ((b.openDays.size + 3) / 4) * 34 + 30).dp), gap = 8.dp) {
            for (d in b.openDays.takeLast(18)) Stamp(LocalDate.ofEpochDay(d), w)
        }
    }
    Text(w.t("Штамп ставится в каждый день, когда книгу открывали.", "A stamp for every day the book was opened."), style = T.Small.copy(textAlign = TextAlign.Center, color = C.muted), modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
}

private fun shelfmark(b: Book): String = "${"АБВГДЕЖЗИКЛМНОПРСТУ"[b.kind.ordinal % 19]}-${b.id}/${b.pages}"

@Composable
private fun Stamp(d: LocalDate, w: Words) {
    val tilt = ((d.toEpochDay() * 37) % 9 - 4).toFloat()
    Text(
        "%02d.%02d.%02d".format(d.dayOfMonth, d.monthValue, d.year % 100), style = T.Card.copy(color = C.stamp.copy(alpha = .85f), fontWeight = FontWeight.Bold, fontFamily = Typewriter),
        modifier = Modifier.padding(4.dp).rotate(tilt).drawBehind {
            drawRect(C.stamp.copy(alpha = .8f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.6.dp.toPx()))
        }.padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** A new edition: a preface, maybe one more chapter. */
@Composable
fun ReissueSheet(h: Home, nav: Nav, id: Long) {
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val w = h.w
    val b = h.shop.book(id) ?: run { nav.pop(); return }
    var preface by rememberSaveable { mutableStateOf("") }
    var chapter by rememberSaveable { mutableStateOf("") }
    Sheet(w.edition(b.edition + 1), { nav.pop() }, w.t("Отмена", "Cancel")) {
        Text(w.t("Книга постояла на полке. Что изменилось с первого издания?", "The book has stood on the shelf a while. What has changed since the first edition?"), style = T.Small)
        Label(w.t("Предисловие к новому изданию", "Preface to the new edition"))
        Field(preface, { preface = it }, w.t("Со времени первого издания автор нашёл зонт…", "Since the first edition the author has found the umbrella…"), minLines = 4, max = 1200)
        Label(w.t("Новая глава в оглавление — если есть", "A new chapter for the contents, if any"))
        Field(chapter, { chapter = it }, w.t("Послесловие гардеробщицы", "The cloakroom attendant's afterword"), max = 90, single = true)
        Gap(16)
        Btn(w.t("В печать", "To press"), Modifier.fillMaxWidth(), enabled = preface.isNotBlank()) {
            c.write { repo.reissue(id, preface, chapter) }
            nav.pop()
            toast.show(w.t("Вышло ${w.edition(b.edition + 1).lowercase()}.", "${w.edition(b.edition + 1)} is out."))
        }
    }
}
