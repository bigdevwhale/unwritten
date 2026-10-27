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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(top + 170.dp)) {
                Canvas(Modifier.fillMaxWidth().height(top + 120.dp)) { drawWindow(sky, t.value) }
                Canvas(Modifier.fillMaxSize()) { drawLamp(size.width / 2, 0f, top.toPx() + 40.dp.toPx(), sky.daylight, t.value) }
            }
            Text(w.t("Лавка ненаписанных книг", "The Unwritten Bookshop"), style = T.Title.copy(color = C.lampHi, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
            Text(w.t("здесь продаются книги, которых нет", "books that don't exist, for sale"), style = T.Small.copy(color = C.lampText, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 6.dp), horizontalArrangement = Arrangement.Center) {
                val faces = listOf(
                    Face(w.t("Зонт, который остался", "The Umbrella That Stayed"), w.t("И. Забытов", "I. M. Forgotten"), Kind.NOVEL, Style.SERIES),
                    Face(w.t("Кошачий для начинающих", "Cat for Beginners"), w.t("Мурзина и Ко", "Purrkins & Co"), Kind.PHRASEBOOK, Style.TRICOLOUR),
                    Face(w.t("Смерть не пришла", "Death Didn't Show"), w.t("Р. Тихонравов", "R. Quietly"), Kind.NOVEL, Style.PULP),
                )
                faces.forEachIndexed { i, f ->
                    Box(Modifier.padding(horizontal = 6.dp).rotate((i - 1) * 4f).shadow(8.dp)) { Cover(f, w, "", Modifier.width(88.dp)) }
                }
            }
            Column(Modifier.padding(16.dp).fillMaxWidth().rotate(-.5f).shadow(8.dp).paper(C.paperLight).padding(20.dp)) {
                Text(
                    w.t("Каждый день к прилавку подходит покупатель с запиской: ему нужна книга, которой не существует. «Роман, где главный герой — забытый зонт». «Разговорник кошачьего».",
                        "Every day a customer comes to the counter with a note: they need a book that doesn't exist. “A novel whose hero is a forgotten umbrella.” “A phrasebook of Cat.”"),
                    style = T.Body,
                )
                Text(
                    w.t("Ты её находишь — то есть пишешь. Но не всю: только название, первую фразу и аннотацию на обороте. Хочешь — эпиграф, оглавление, страницу из середины. Книга встаёт на полку, к ней заходят критики, а через пару недель её возвращает читатель.",
                        "You find it, which is to say you write it. Not all of it: just the title, the first line and the blurb on the back. An epigraph, the contents, a page from the middle if you like. The book goes on the shelf, critics drop by, and a couple of weeks later a reader brings it back."),
                    style = T.Body, modifier = Modifier.padding(top = 10.dp),
                )
                Text(w.t("Как будет называться твоё издательство?", "What will your imprint be called?"), style = T.Label, modifier = Modifier.padding(top = 16.dp))
                Field(imprint, { imprint = it }, w.imprint(""), single = true, max = 40)
                Text(w.t("За окном — настоящее время суток. Город для настоящей погоды можно выбрать в настройках.", "Outside the window it's the real time of day. Pick a town in the settings for the real weather too."),
                    style = T.Small.copy(fontSize = 12.5.sp), modifier = Modifier.padding(top = 10.dp))
            }
            Btn(
                w.t("Открыть лавку", "Open the shop"), Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 28.dp),
            ) { c.write { this.settings.update { it.copy(onboarded = true, imprint = imprint.trim()) } } }
        }
    }
}
