package app.unwritten.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.unwritten.BuildConfig
import app.unwritten.container
import app.unwritten.domain.Place
import app.unwritten.domain.Sky
import app.unwritten.ui.Home
import app.unwritten.ui.Words
import app.unwritten.ui.components.Btn
import app.unwritten.ui.components.Chip
import app.unwritten.ui.components.Field
import app.unwritten.ui.components.Flow
import app.unwritten.ui.components.Gap
import app.unwritten.ui.components.Kind
import app.unwritten.ui.components.Sheet
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The StillCraft bot: deep links file a bug or an idea for this app, or follow its releases. */
object Community {
    private const val BOT = "https://t.me/ArcaneWhisperBot?start="
    const val SLUG = "unwritten"
    const val BUG = "${BOT}bug_$SLUG"
    const val IDEA = "${BOT}wish_$SLUG"
    const val FOLLOW = "${BOT}f_$SLUG"
    const val CHAT = "https://t.me/+Q4gFEDk8O842OGVi"
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxWidth().shadow(2.dp).background(C.paperLight).padding(16.dp), content = content)

@Composable
fun SettingsSheet(h: Home, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val w = h.w
    val st = h.settings

    Sheet(w.t("Настройки", "Settings"), onClose, w.t("Закрыть", "Close")) {
        Card {
            Text(w.t("Язык", "Language"), style = T.Heading)
            Flow(Modifier.padding(top = 8.dp)) {
                Chip("Русский", h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru")) }
                Chip("English", !h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }
            }
        }
        Gap(10)
        Card {
            Text(w.t("За окном", "Outside the window"), style = T.Heading)
            Text(
                w.t("Время суток за окном всегда настоящее. Погода тоже станет настоящей, если выбрать город: она берётся из Open-Meteo (или MET Norway), отправляются только координаты города.",
                    "The time of day outside is always the real one. The weather becomes real too once you pick a town: it comes from Open-Meteo (or MET Norway), and only the town's coordinates are sent."),
                style = T.Small,
            )
            st.place?.let { p ->
                Text("${p.name}${if (p.region.isNotBlank()) ", ${p.region}" else ""}", style = T.BodyB, modifier = Modifier.padding(top = 8.dp))
                Text(w.t("Убрать город — будет своя погода", "Remove the town: the shop's own weather"), style = T.Small.copy(color = C.accent),
                    modifier = Modifier.clickable { c.write { settings.update { it.copy(place = null, weather = null) } } }.padding(vertical = 4.dp))
            }
            PlacePicker(w) { picked -> c.write { settings.update { it.copy(place = picked, weather = null) }; refreshWeather(force = true) } }
        }
        Gap(10)
        Card {
            Text(w.t("Издательство", "Imprint"), style = T.Heading)
            Text(w.t("Название на каждой обложке и в каждом зине.", "The name on every cover and every zine."), style = T.Small)
            var name by remember { mutableStateOf(st.imprint) }
            Field(name, { name = it }, w.imprint(""), Modifier.padding(top = 6.dp), single = true, max = 40)
            if (name.trim() != st.imprint) Btn(w.t("Сохранить", "Save"), Modifier.padding(top = 6.dp), Kind.GHOST) { c.write { settings.update { it.copy(imprint = name.trim()) } } }
        }
        Gap(10)
        Card {
            Text(w.t("Как это устроено", "How it works"), style = T.Heading)
            Text(w.t("Заглянуть в лавку ещё раз, как в первый день ›", "Step into the shop again, as on the first day ›"), style = T.Small.copy(color = C.accent),
                modifier = Modifier.clickable { c.write { settings.update { it.copy(onboarded = false) } }; onClose() }.padding(top = 4.dp))
        }
        Gap(10)
        CommunityLinks(w)
        Gap(10)
        Card {
            Text(w.t("О «Ненаписанном»", "About Unwritten"), style = T.Heading)
            Text(w.t("Лавка книг, которых нет. Каждый день покупатель просит книгу, которой не существует, и ты её находишь — пишешь только то, что вокруг текста. Без рекламы и без аккаунта; всё хранится на телефоне. Интернет нужен только для погоды за окном.",
                "A shop of books that don't exist. Every day a customer asks for a book nobody wrote, and you find it, writing only what surrounds the text. No ads, no account; everything stays on the phone. The internet is used only for the weather outside the window."), style = T.Small)
            Text(w.t("Версия ${BuildConfig.VERSION_NAME} · шрифты Lora, Caveat, Forum, Inter, PT Sans Narrow, PT Mono (OFL) · исходники на GitHub", "Version ${BuildConfig.VERSION_NAME} · Lora, Caveat, Forum, Inter, PT Sans Narrow, PT Mono fonts (OFL) · source on GitHub"),
                style = T.Small.copy(color = C.accent),
                modifier = Modifier.padding(top = 8.dp).clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bigdevwhale/unwritten"))) } })
        }
        Gap(20)
    }
}

/** A search for the town whose sky goes in the window. */
@Composable
fun PlacePicker(w: Words, onPicked: (Place) -> Unit) {
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var note by remember { mutableStateOf<String?>(null) }
    fun search() {
        if (q.isBlank()) return
        note = w.t("Ищу…", "Searching…")
        scope.launch {
            val r = runCatching { withContext(Dispatchers.IO) { Sky.search(q, if (w.ru) "ru" else "en") } }
            results = r.getOrDefault(emptyList())
            note = when { r.isFailure -> w.t("Нет связи. Попробуй позже.", "No connection. Try later."); results.isEmpty() -> w.t("Такого города не нашлось.", "No such town."); else -> null }
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Field(q, { q = it }, w.t("Город…", "Town…"), Modifier.weight(1f), single = true, max = 60, onDone = { search() })
        Btn(w.t("Найти", "Find"), Modifier.padding(start = 8.dp).width(96.dp), Kind.GHOST) { search() }
    }
    note?.let { Text(it, style = T.Small) }
    for (p in results) {
        Text("${p.name}${if (p.region.isNotBlank()) " · ${p.region}" else ""}", style = T.Body,
            modifier = Modifier.fillMaxWidth().clickable { onPicked(p); results = emptyList(); q = "" }.padding(vertical = 8.dp))
    }
}

/** Report a bug, suggest an idea, follow updates, join the chat; each opens Telegram. */
@Composable
private fun CommunityLinks(w: Words) {
    val ctx = LocalContext.current
    fun open(url: String) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    Card {
        Text(w.t("Сообщество", "Community"), style = T.Heading)
        Text(w.t("Приложение делается вместе с теми, кто им пользуется. Всё идёт через бота StillCraft в Telegram.", "The app is made together with the people who use it. It all goes through the StillCraft bot on Telegram."),
            style = T.Small, modifier = Modifier.padding(bottom = 4.dp))
        for ((icon, label, sub, url) in listOf(
            Link("✎", w.t("Сообщить о баге", "Report a bug"), w.t("что сломалось и как повторить", "what broke and how to repeat it"), Community.BUG),
            Link("✦", w.t("Предложить идею", "Suggest an idea"), w.t("заказ, обложка, функция", "a request, a cover, a feature"), Community.IDEA),
            Link("✉", w.t("Подписаться на обновления", "Follow updates"), w.t("бот напишет, когда выйдет версия", "the bot writes when a version is out"), Community.FOLLOW),
            Link("❦", w.t("Чат StillCraft", "StillCraft chat"), w.t("все приложения и люди", "all the apps and people"), Community.CHAT),
        )) {
            Row(Modifier.fillMaxWidth().clickable { open(url) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = T.Heading.copy(color = C.accent), modifier = Modifier.width(34.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = T.BodyB)
                    Text(sub, style = T.Small)
                }
                Text("›", style = T.Heading.copy(color = C.accent))
            }
        }
    }
}

private data class Link(val icon: String, val label: String, val sub: String, val url: String)
