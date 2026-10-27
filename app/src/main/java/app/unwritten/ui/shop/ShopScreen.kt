package app.unwritten.ui.shop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.container
import app.unwritten.domain.Book
import app.unwritten.domain.Rule
import app.unwritten.domain.Rules
import app.unwritten.domain.hash
import app.unwritten.ui.Home
import app.unwritten.ui.Nav
import app.unwritten.ui.Overlay
import app.unwritten.ui.Page
import app.unwritten.ui.art.Cover
import app.unwritten.ui.art.Deco
import app.unwritten.ui.art.Face
import app.unwritten.ui.art.decoWidth
import app.unwritten.ui.art.drawBell
import app.unwritten.ui.art.drawCup
import app.unwritten.ui.art.drawDeco
import app.unwritten.ui.art.drawLamp
import app.unwritten.ui.art.drawMotes
import app.unwritten.ui.art.drawSpine
import app.unwritten.ui.art.drawWindow
import app.unwritten.ui.art.drawWood
import app.unwritten.ui.art.spineColors
import app.unwritten.ui.components.Btn
import app.unwritten.ui.components.Kind
import app.unwritten.ui.components.LocalToast
import app.unwritten.ui.components.paper
import app.unwritten.ui.components.rememberSeconds
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

/** The shop: a window onto the real sky, the lamp, two shelves, the counter with today's customer, the bargain box. */
@Composable
fun ShopScreen(h: Home, nav: Nav) {
    val t = rememberSeconds()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(h, nav, t)
        Shelf(h.w.t("Романы и повести", "Fiction"), h.shop.fiction, 0, h, nav, t)
        Spacer(Modifier.height(14.dp))
        Shelf(h.w.t("Справочный отдел", "Reference"), h.shop.reference, 1, h, nav, t)
        h.returned?.let { Returned(h, nav, it) }
        Counter(h, nav, t)
        Crate(h, nav)
        Text(
            h.w.t("На полках ${h.w.books(h.shop.shelved.size)} · ${h.w.pages(h.shop.pages)}, которых нет", "${h.w.books(h.shop.shelved.size)} on the shelves · ${h.w.pages(h.shop.pages)} that don't exist"),
            style = T.Small.copy(color = C.lampText.copy(alpha = .7f), textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 18.dp, bottom = 22.dp, start = 16.dp, end = 16.dp),
        )
    }
}

@Composable
private fun Header(h: Home, nav: Nav, t: State<Float>) {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val winH = top + 132.dp
    val w = h.w
    val sky = h.sky
    Box(Modifier.fillMaxWidth().height(winH + 106.dp)) {
        Canvas(Modifier.fillMaxWidth().height(winH)) { drawWindow(sky, t.value) }
        // Light from the window falls into the shop by day; a rosy cast at dawn and dusk.
        Canvas(Modifier.fillMaxSize()) {
            val spill = when {
                sky.daylight >= .6f -> Color(0xFFDCEBFF).copy(alpha = .16f * sky.daylight)
                sky.daylight > 0f -> Color(0xFFF0A078).copy(alpha = .10f)
                else -> Color.Transparent
            }
            if (spill != Color.Transparent) drawRect(Brush.radialGradient(listOf(spill, Color.Transparent), Offset(size.width / 2, winH.toPx()), size.width * .8f), Offset(0f, winH.toPx()), Size(size.width, size.height - winH.toPx()))
            drawMotes(Offset(size.width / 2, winH.toPx() * .55f), t.value, sky.daylight)
            drawLamp(size.width / 2, 0f, top.toPx() + 50.dp.toPx(), sky.daylight, t.value)
        }
        // The two corner buttons over the frame: the card catalogue and the settings.
        Row(Modifier.fillMaxWidth().padding(top = top + 14.dp, start = 18.dp, end = 18.dp)) {
            IconBtn(onClick = { nav.push(Overlay.Catalog) }) {
                val s = 1.6.dp.toPx()
                drawRect(C.lampHi, Offset(size.width * .2f, size.height * .3f), Size(size.width * .6f, size.height * .45f), style = Stroke(s))
                drawLine(C.lampHi, Offset(size.width * .2f, size.height * .52f), Offset(size.width * .8f, size.height * .52f), s)
                drawLine(C.lampHi, Offset(size.width * .42f, size.height * .42f), Offset(size.width * .58f, size.height * .42f), s, StrokeCap.Round)
                drawLine(C.lampHi, Offset(size.width * .42f, size.height * .64f), Offset(size.width * .58f, size.height * .64f), s, StrokeCap.Round)
            }
            Spacer(Modifier.weight(1f))
            IconBtn(onClick = { nav.push(Overlay.Settings) }) {
                val s = 1.6.dp.toPx()
                for ((i, k) in listOf(.35f, .62f, .48f).withIndex()) {
                    val y = size.height * (.32f + i * .18f)
                    drawLine(C.lampHi, Offset(size.width * .22f, y), Offset(size.width * .78f, y), s, StrokeCap.Round)
                    drawCircle(Color(0xFF3A2818), 3.2.dp.toPx(), Offset(size.width * k, y))
                    drawCircle(C.lampHi, 3.2.dp.toPx(), Offset(size.width * k, y), style = Stroke(s))
                }
            }
        }
        // What the weather is, on the sill.
        val label = buildString {
            h.settings.place?.let { append(it.name).append(" · ") }
            sky.temp?.let { val r = it.roundToInt(); append(if (r > 0) "+$r°" else "$r°").append(" · ") }
            append(w.weather(sky.weather))
            if (h.settings.place == null) append(w.t(" · выбрать город", " · pick a town"))
        }
        Text(
            label, style = T.Small.copy(color = Color(0xFFFFF3DC), fontSize = 11.5.sp, lineHeight = 14.sp),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = winH - 34.dp, end = 22.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0x99281A10)).clickable { nav.push(Overlay.Settings) }.padding(horizontal = 9.dp, vertical = 3.dp),
        )
        Sign(h, Modifier.align(Alignment.TopCenter).padding(top = winH + 16.dp), t)
    }
}

@Composable
private fun IconBtn(onClick: () -> Unit, draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x99281A10)).clickable(onClick = onClick)) {
        Canvas(Modifier.fillMaxSize(), onDraw = draw)
    }
}

/** The hanging sign with the shop's name, swaying a little. */
@Composable
private fun Sign(h: Home, modifier: Modifier, t: State<Float>) {
    Box(modifier.fillMaxWidth(.84f)) {
        Box(
            Modifier.fillMaxWidth().graphicsLayer {
                rotationZ = sin(t.value * .9f) * .7f
                transformOrigin = TransformOrigin(.5f, -.6f)
            },
        ) {
            Canvas(Modifier.matchParentSize()) {
                val chain = Color(0xFFB08A4A)
                for (x in listOf(size.width * .22f, size.width * .78f)) {
                    var y = -22.dp.toPx()
                    while (y < 0) { drawCircle(chain, 1.6.dp.toPx(), Offset(x, y), style = Stroke(1.dp.toPx())); y += 3.4.dp.toPx() }
                }
            }
            Column(
                Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).drawBehind {
                    drawWood(Color(0xFF6E4A2C), Offset.Zero, size, 4)
                    drawRect(Color(0xFF8A603A), style = Stroke(2.dp.toPx()))
                    drawRect(Color(0xFF5A3B22), Offset(3.dp.toPx(), 3.dp.toPx()), Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()), style = Stroke(1.5.dp.toPx()))
                }.padding(vertical = 10.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(h.w.t("Лавка ненаписанных книг", "The Unwritten Bookshop"), style = T.Title.copy(fontSize = 22.sp, lineHeight = 26.sp, color = C.lampHi, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, textAlign = TextAlign.Center))
                Text(h.w.t("здесь продаются книги, которых нет", "books that don't exist, for sale"), style = T.Small.copy(color = C.lampText, fontSize = 12.5.sp))
            }
        }
    }
}

/** A shelf: back boards, books standing with an ornament here and there, a paper label on the edge. */
@Composable
private fun Shelf(label: String, books: List<Book>, which: Int, h: Home, nav: Nav, t: State<Float>) {
    val decos = Deco.entries
    Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth().height(196.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val side = 8.dp.toPx(); val board = 16.dp.toPx()
            drawWood(C.shelfBack, Offset(side, 0f), Size(size.width - 2 * side, size.height - board), 30 + which, vertical = true)
            drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .45f), Color.Transparent), 0f, 30.dp.toPx()), Offset(side, 0f), Size(size.width - 2 * side, 30.dp.toPx()))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .35f)), size.height - board - 40.dp.toPx(), size.height - board), Offset(side, size.height - board - 40.dp.toPx()), Size(size.width - 2 * side, 40.dp.toPx()))
            drawWood(Color(0xFF5A3A22), Offset(0f, 0f), Size(side, size.height), 40 + which, vertical = true)
            drawWood(Color(0xFF5A3A22), Offset(size.width - side, 0f), Size(side, size.height), 50 + which, vertical = true)
            drawWood(C.wood, Offset(0f, size.height - board), Size(size.width, board), 60 + which)
            drawRect(Color(0xFF8A603A), Offset(0f, size.height - board), Size(size.width, 2.dp.toPx()))
        }
        Row(
            Modifier.fillMaxSize().padding(start = 14.dp, end = 12.dp, bottom = 16.dp).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            DecoItem(decos[(which * 2) % decos.size], t)
            if (books.isEmpty()) {
                Text(h.w.t("полка ждёт", "the shelf is waiting"), style = T.Small.copy(color = C.lampText.copy(alpha = .55f)), modifier = Modifier.padding(horizontal = 24.dp, vertical = 70.dp))
            }
            books.forEachIndexed { i, b ->
                if (i > 0 && i % 5 == 0) DecoItem(decos[(i / 5 + which * 3 + 1) % decos.size], t)
                Spine(b, lean = i == books.lastIndex && books.size > 2 && b.id != nav.fresh, fresh = b.id == nav.fresh) { nav.push(Overlay.Book(b.id)) }
            }
            Spacer(Modifier.width(6.dp))
            DecoItem(decos[(which * 2 + 1) % decos.size], t)
        }
        Text(
            label, style = T.Small.copy(color = C.woodDark, fontSize = 11.sp, lineHeight = 13.sp),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.5.dp).shadow(1.dp).background(Color(0xFFE9DCC0)).padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun DecoItem(k: Deco, t: State<Float>) {
    Canvas(Modifier.padding(horizontal = 4.dp).size(decoWidth(k).dp, 76.dp)) { drawDeco(k, t.value) }
}

/** A book standing on the shelf: thicker for more pages, taller or shorter by its title. */
@Composable
private fun Spine(b: Book, lean: Boolean, fresh: Boolean, onClick: () -> Unit) {
    val f = remember(b.title, b.style, b.kind, b.edition) { Face(b) }
    val width: Dp = (10f + b.pages / 22f).coerceIn(16f, 46f).dp
    val height: Dp = (128 + hash(b.title) % 40).dp
    val tm = rememberTextMeasurer(cacheSize = 4)
    val drop = remember { Animatable(0f) }
    LaunchedEffect(fresh) { if (fresh) { drop.snapTo(-260f); drop.animateTo(0f, tween(700)) } }
    Canvas(
        Modifier.padding(start = if (lean) 16.dp else 0.dp).size(width, height)
            .offset { IntOffset(0, (drop.value * density).roundToInt()) }
            .graphicsLayer {
                if (lean) { rotationZ = -9f; transformOrigin = TransformOrigin(0f, 1f) }
            }
            .shadow(3.dp, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .clickable(onClick = onClick),
    ) { drawSpine(f, tm) }
}

/** A book come back from a reader, nothing yet in its margins. */
@Composable
private fun Returned(h: Home, nav: Nav, b: Book) {
    val c = LocalContext.current.container
    val w = h.w
    Row(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp).fillMaxWidth().rotate(.6f).shadow(6.dp).paper(C.paperLight).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(Face(b), w, h.imprint, Modifier.width(52.dp))
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(w.t("Вернули книгу", "A book came back"), style = T.BodyB)
            Text(
                w.t("«${b.title}» вернулась от читателя. На полях пусто — перечитай её как чужую и оставь пометку.", "“${b.title}” is back from a reader. The margins are empty: read it as someone else's and leave a note."),
                style = T.Small,
            )
            Btn(w.t("Перечитать", "Read it again"), Modifier.padding(top = 8.dp), Kind.GHOST) {
                c.write { settings.update { it.copy(returnSeen = h.today) } }
                nav.push(Overlay.Book(b.id, Page.OPEN))
            }
        }
    }
}

/** The counter: today's customer and their note, the bell, a cup of tea. */
@Composable
private fun Counter(h: Home, nav: Nav, t: State<Float>) {
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val w = h.w
    val scope = rememberCoroutineScope()
    val ring = remember { Animatable(1f) }
    val visit = h.visit
    Box(
        Modifier.padding(start = 12.dp, end = 12.dp, top = 26.dp).fillMaxWidth().shadow(10.dp, RoundedCornerShape(10.dp, 10.dp, 4.dp, 4.dp))
            .clip(RoundedCornerShape(10.dp, 10.dp, 4.dp, 4.dp)).drawBehind {
                drawWood(C.counter, Offset.Zero, size, 77)
                drawRect(Brush.verticalGradient(listOf(Color(0x1AFFD296), Color.Transparent), 0f, size.height * .4f))
                drawRect(Color(0xFFA0754A), Offset.Zero, Size(size.width, 3.dp.toPx()))
                drawRect(Color(0xFF5A3A22), Offset(0f, size.height - 6.dp.toPx()), Size(size.width, 6.dp.toPx()))
            },
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.t("За прилавком", "At the counter"), style = T.Italic.copy(color = C.lampHi), modifier = Modifier.weight(1f))
                Canvas(
                    Modifier.size(40.dp, 32.dp).clickable {
                        if (h.rings >= Rules.RINGS_A_DAY) toast.show(w.t("Больше никого. В лавке тихо — до завтра.", "Nobody else. The shop is quiet till tomorrow."))
                        else {
                            scope.launch { ring.snapTo(0f); ring.animateTo(1f, tween(700)) }
                            c.write { settings.update { it.copy(ringDay = h.today, rings = h.rings + 1) } }
                        }
                    },
                ) { drawBell(ring.value) }
            }
            Box(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                if (visit != null) {
                    Column(
                        Modifier.padding(end = 58.dp).rotate(-1.4f).shadow(5.dp).background(Brush.verticalGradient(listOf(Color(0xFFFBF4E2), Color(0xFFF1E6CC))))
                            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
                    ) {
                        Text(visit.request.text(h.ru), style = T.Note)
                        if (visit.request.rule != Rule.NONE) Text(w.rule(visit.request.rule), style = T.Small.copy(color = C.accent, fontSize = 12.5.sp), modifier = Modifier.padding(top = 6.dp))
                        Text("— ${w.visitors[visit.visitor]}", style = T.Small.copy(color = C.muted), modifier = Modifier.padding(top = 6.dp))
                    }
                    // The pin.
                    Box(Modifier.padding(start = 110.dp, top = 0.dp).size(13.dp).offset(y = (-5).dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFFF8B78), C.pin))))
                }
                Canvas(Modifier.align(Alignment.TopEnd).padding(top = 10.dp).size(52.dp, 56.dp)) { drawCup(t.value) }
            }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn(w.t("Найти на полке", "Find it on the shelf"), Modifier.weight(1.4f), enabled = visit != null) { nav.push(Overlay.Write(visit)) }
                Btn(w.t("Без заказа", "For nobody"), Modifier.weight(1f), Kind.LIGHT) { nav.push(Overlay.Write(null)) }
            }
        }
    }
}

/** The bargain box by the door: books nobody throws away. */
@Composable
private fun Crate(h: Home, nav: Nav) {
    val w = h.w
    Column(
        Modifier.padding(start = 12.dp, end = 12.dp, top = 24.dp).fillMaxWidth().shadow(8.dp, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).drawBehind {
            val slat = 22.dp.toPx(); val gap = 3.dp.toPx()
            var y = 0f; var i = 0
            while (y < size.height) {
                drawWood(Color(0xFF8C6A44), Offset(0f, y), Size(size.width, slat), 90 + i)
                drawRect(Color(0xFF4A3420), Offset(0f, y + slat), Size(size.width, gap))
                y += slat + gap; i++
            }
            drawRect(Color(0xFF6E5032), style = Stroke(4.dp.toPx()))
        }.padding(14.dp),
    ) {
        Text(
            w.t("Уценка · всё по 10 коп.", "Bargain box · 10¢ each"), style = T.Note.copy(fontSize = 21.sp, lineHeight = 22.sp, color = Color(0xFF7A2E1C)),
            modifier = Modifier.rotate(-2f).shadow(2.dp).background(Color(0xFFF4EAD0)).padding(horizontal = 10.dp, vertical = 1.dp),
        )
        Spacer(Modifier.height(10.dp))
        if (h.shop.bin.isEmpty()) {
            Text(w.t("Ящик пуст. Неудачные книги здесь не выбрасывают — их уценивают.", "Empty. Books that didn't work out aren't thrown away here; they're marked down."),
                style = T.Small.copy(color = Color(0xFFF1E2C4)))
        } else {
            app.unwritten.ui.components.Flow(gap = 4.dp) {
                for (b in h.shop.bin) {
                    val (bg, fg) = spineColors(Face(b))
                    Box(
                        Modifier.shadow(2.dp).background(bg).clickable { nav.push(Overlay.Book(b.id)) }.padding(horizontal = 8.dp, vertical = 2.dp),
                    ) { Text(b.title, style = T.Small.copy(color = fg, fontSize = 11.sp, lineHeight = 13.sp), maxLines = 1) }
                }
            }
        }
    }
}
