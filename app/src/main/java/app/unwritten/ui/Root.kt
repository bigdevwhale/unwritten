package app.unwritten.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.unwritten.container
import app.unwritten.data.Settings
import app.unwritten.domain.Requests
import app.unwritten.domain.Shop
import app.unwritten.domain.Sky
import app.unwritten.domain.SkyNow
import app.unwritten.domain.Visit
import app.unwritten.domain.day
import app.unwritten.ru
import app.unwritten.ui.art.wallpaper
import app.unwritten.ui.book.BookSheet
import app.unwritten.ui.book.ReissueSheet
import app.unwritten.ui.catalog.CatalogSheet
import app.unwritten.ui.components.LocalToast
import app.unwritten.ui.components.ToastHost
import app.unwritten.ui.components.Toaster
import app.unwritten.ui.onboarding.Onboarding
import app.unwritten.ui.settings.SettingsSheet
import app.unwritten.ui.shop.ShopScreen
import app.unwritten.ui.write.WriteSheet
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

/** Everything a screen reads, at one moment. */
class Home(val shop: Shop, val settings: Settings, val w: Words, val now: Long, val requests: Requests) {
    val ru get() = w.ru
    val zone: ZoneId = ZoneId.systemDefault()
    val today: Long get() = now.day(zone)
    val sky: SkyNow by lazy { Sky.now(Instant.ofEpochMilli(now).atZone(zone), settings.place, settings.weather) }
    val rings: Int get() = if (settings.ringDay == today) settings.rings else 0
    val visit: Visit? by lazy { requests.visit(today, rings, shop.served, w.visitors.size) }
    val returned by lazy { if (settings.returnSeen == today) null else shop.returned(today, zone) }
    val imprint: String get() = settings.imprint
}

enum class Page { COVER, BACK, OPEN, CARD }

sealed interface Overlay {
    /** Writing a book: for today's customer, or for nobody. */
    data class Write(val visit: Visit?) : Overlay
    data class Book(val id: Long, val page: Page = Page.COVER) : Overlay
    data class Reissue(val id: Long) : Overlay
    data object Catalog : Overlay
    data object Settings : Overlay
}

/** How screens open sheets over the shop. [fresh] is the book just shelved, so it can slide into place. */
class Nav(val overlays: SnapshotStateList<Overlay>) {
    var fresh by mutableLongStateOf(0L)
    fun push(o: Overlay) = overlays.add(o)
    fun pop() { if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex) }
}

@Composable
fun ShopRoot(settings: Settings) {
    val ctx = LocalContext.current
    val c = ctx.container
    val shop by c.repo.shop.collectAsStateWithLifecycle()
    val words = remember(ctx.ru) { Words(ctx.ru) }
    val toaster = remember { Toaster() }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    if (!settings.onboarded) {
        CompositionLocalProvider(LocalToast provides toaster) { Box(Modifier.fillMaxSize()) { Onboarding(words, settings); ToastHost(toaster) } }
        return
    }
    val life = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        life.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            c.repo.refresh()
            c.refreshWeather()
            while (true) { now = System.currentTimeMillis(); delay(30_000) }
        }
    }
    val s = shop ?: run { Box(Modifier.fillMaxSize().wallpaper()); return }
    val home = Home(s, settings, words, now, c.requests)
    val overlays = remember { mutableStateListOf<Overlay>() }
    val nav = remember { Nav(overlays) }

    CompositionLocalProvider(LocalToast provides toaster) {
        Box(Modifier.fillMaxSize().wallpaper(home.sky.daylight)) {
            ShopScreen(home, nav)
            when (val o = overlays.lastOrNull()) {
                is Overlay.Write -> WriteSheet(home, nav, o.visit)
                is Overlay.Book -> BookSheet(home, nav, o.id, o.page)
                is Overlay.Reissue -> ReissueSheet(home, nav, o.id)
                Overlay.Catalog -> CatalogSheet(home, nav)
                Overlay.Settings -> SettingsSheet(home) { nav.pop() }
                null -> Unit
            }
            ToastHost(toaster)
        }
    }
}
