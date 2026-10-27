package app.unwritten.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.domain.Kind
import app.unwritten.ui.Home
import app.unwritten.ui.Nav
import app.unwritten.ui.Overlay
import app.unwritten.ui.art.Cover
import app.unwritten.ui.art.Face
import app.unwritten.ui.components.Chip
import app.unwritten.ui.components.Field
import app.unwritten.ui.components.Flow
import app.unwritten.ui.components.Gap
import app.unwritten.ui.components.Sheet
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import java.time.Instant

/** The card catalogue: every book of the shop, newest first, found by a word or a kind. */
@Composable
fun CatalogSheet(h: Home, nav: Nav) {
    val w = h.w
    var q by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf<Kind?>(null) }
    val shop = h.shop
    Sheet(w.t("Каталог", "Catalogue"), { nav.pop() }, w.t("Закрыть", "Close")) {
        Text(
            w.t("${w.books(shop.books.size)}, ${w.pages(shop.pages)}, которых нет. Критики заходили ${shop.reviews.size} раз.",
                "${w.books(shop.books.size)}, ${w.pages(shop.pages)} that don't exist. Critics have called ${shop.reviews.size} times."),
            style = T.Small,
        )
        Gap(8)
        Field(q, { q = it }, w.t("Найти по слову…", "Find by a word…"), single = true, max = 40)
        val kinds = shop.books.map { it.kind }.distinct().sortedBy { it.ordinal }
        if (kinds.size > 1) Flow(Modifier.padding(top = 6.dp)) {
            Chip(w.t("все", "all"), kind == null) { kind = null }
            for (k in kinds) Chip(w.kind(k), k == kind) { kind = if (kind == k) null else k }
        }
        Gap(10)
        val list = shop.books.reversed().filter { b ->
            (kind == null || b.kind == kind) && (q.isBlank() || listOf(b.title, b.author, b.blurb, b.first, b.note).any { it.contains(q.trim(), ignoreCase = true) })
        }
        if (list.isEmpty()) Text(w.t("Таких карточек нет.", "No such cards."), style = T.Small)
        for (b in list) {
            Row(
                Modifier.padding(vertical = 5.dp).fillMaxWidth().shadow(2.dp).background(Color(0xFFF1E6C9)).drawBehind {
                    drawLine(Color(0xFFD9A0A0), Offset(0f, 30.dp.toPx()), Offset(size.width, 30.dp.toPx()), 1.dp.toPx())
                    drawCircle(Color(0xFF6E5032), 4.dp.toPx(), Offset(size.width / 2, size.height - 9.dp.toPx()))
                }.clickable { nav.push(Overlay.Book(b.id)) }.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(b.author.ifBlank { "—" }, style = T.Card.copy(fontSize = 12.sp))
                    Text(b.title, style = T.Card.copy(fontSize = 14.sp, lineHeight = 19.sp), modifier = Modifier.padding(top = 8.dp))
                    Text(
                        "${w.kind(b.kind)} · ${b.pages} ${w.t("с.", "pp.")} · ${w.date(Instant.ofEpochMilli(b.created).atZone(h.zone).toLocalDate())}" +
                            (if (b.bargain) w.t(" · уценка", " · bargain") else "") + (if (b.edition > 1) " · ${w.t("изд.", "ed.")} ${b.edition}" else ""),
                        style = T.Card.copy(fontSize = 11.sp, color = C.muted), modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Cover(Face(b), w, h.imprint, Modifier.width(40.dp).padding(start = 8.dp))
            }
        }
        Gap(24)
    }
}
