package app.unwritten.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.container
import app.unwritten.data.Settings
import app.unwritten.domain.Kind
import app.unwritten.domain.Sky
import app.unwritten.domain.Style
import app.unwritten.ui.Words
import app.unwritten.ui.art.Cover
import app.unwritten.ui.art.Face
import app.unwritten.ui.art.drawLamp
import app.unwritten.ui.art.drawWindow
import app.unwritten.ui.art.wallpaper
import app.unwritten.ui.components.Btn
import app.unwritten.ui.components.Field
import app.unwritten.ui.components.paper
import app.unwritten.ui.components.rememberSeconds
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import java.time.ZonedDateTime

/** The first evening: what this shop is, and the name of its imprint. */
@Composable
fun Onboarding(w: Words, settings: Settings) {
    val c = LocalContext.current.container
    val t = rememberSeconds()
    val sky = remember { Sky.now(ZonedDateTime.now(), settings.place, settings.weather) }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var imprint by remember { mutableStateOf(settings.imprint) }
    Box(Modifier.fillMaxSize().wallpaper(sky.daylight)) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = top + 70.dp)) {
                Canvas(Modifier.fillMaxWidth().fillMaxHeight(.8f)) { drawWindow(sky, t.value) }
                Canvas(Modifier.fillMaxSize()) { drawLamp(size.width / 2, 0f, top.toPx() + 14.dp.toPx(), sky.daylight, t.value) }
            }
            Text(w.t("Лавка ненаписанных книг", "The Unwritten Bookshop"), style = T.Title.copy(color = C.lampHi, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
            Text(w.t("здесь продаются книги, которых нет", "books that don't exist, for sale"), style = T.Small.copy(color = C.lampText, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 2.dp), horizontalArrangement = Arrangement.Center) {
                val faces = listOf(
                    Face(w.t("Зонт, который остался", "The Umbrella That Stayed"), w.t("И. Забытов", "I. M. Forgotten"), Kind.NOVEL, Style.SERIES),
                    Face(w.t("Кошачий для начинающих", "Cat for Beginners"), w.t("Мурзина и Ко", "Purrkins & Co"), Kind.PHRASEBOOK, Style.TRICOLOUR),
                    Face(w.t("Смерть не пришла", "Death Didn't Show"), w.t("Р. Тихонравов", "R. Quietly"), Kind.NOVEL, Style.PULP),
                )
                faces.forEachIndexed { i, f ->
                    Box(Modifier.padding(horizontal = 6.dp).rotate((i - 1) * 4f).shadow(8.dp)) { Cover(f, w, "", Modifier.width(76.dp)) }
                }
            }
            Column(Modifier.padding(16.dp).fillMaxWidth().rotate(-.5f).shadow(8.dp).paper(C.paperLight).padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    w.t("Каждый день к прилавку подходит покупатель с запиской: ему нужна книга, которой не существует. «Роман, где главный герой — забытый зонт».",
                        "Every day a customer comes to the counter with a note: they need a book that doesn't exist. “A novel whose hero is a forgotten umbrella.”"),
                    style = T.Body.copy(fontSize = 15.sp, lineHeight = 21.sp),
                )
                Text(
                    w.t("Ты её находишь — то есть пишешь. Только название, первую фразу и аннотацию. Книга встаёт на полку, заходят критики, а потом её возвращает читатель.",
                        "You find it, which is to say you write it: just the title, the first line and the blurb. The book goes on the shelf, critics drop by, and later a reader brings it back."),
                    style = T.Body.copy(fontSize = 15.sp, lineHeight = 21.sp), modifier = Modifier.padding(top = 8.dp),
                )
                Text(w.t("Как будет называться твоё издательство?", "What will your imprint be called?"), style = T.Label, modifier = Modifier.padding(top = 10.dp))
                Field(imprint, { imprint = it }, w.imprint(""), single = true, max = 40)
                Text(w.t("За окном — настоящее время суток. Город для настоящей погоды можно выбрать в настройках.", "Outside the window it's the real time of day. Pick a town in the settings for the real weather too."),
                    style = T.Small.copy(fontSize = 12.sp, lineHeight = 16.sp), modifier = Modifier.padding(top = 6.dp))
            }
            Btn(
                w.t("Открыть лавку", "Open the shop"), Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 16.dp),
            ) { c.write { this.settings.update { it.copy(onboarded = true, imprint = imprint.trim()) } } }
        }
    }
}
