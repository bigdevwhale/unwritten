package app.unwritten

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import app.unwritten.data.DemoSeed
import app.unwritten.domain.Place
import app.unwritten.domain.Weather
import app.unwritten.domain.WeatherNow
import app.unwritten.ui.Home
import app.unwritten.ui.Nav
import app.unwritten.ui.Page
import app.unwritten.ui.Words
import app.unwritten.ui.art.wallpaper
import app.unwritten.ui.book.BookSheet
import app.unwritten.ui.catalog.CatalogSheet
import app.unwritten.ui.components.LocalToast
import app.unwritten.ui.components.Toaster
import app.unwritten.ui.onboarding.Onboarding
import app.unwritten.ui.settings.SettingsSheet
import app.unwritten.ui.shop.ShopScreen
import app.unwritten.ui.theme.ShopTheme
import app.unwritten.ui.write.WriteSheet
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/** Every main screen over the demo history, drawn to PNGs (run with `-Prender`), English and Russian. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }

    /** The shop at [hour] today, with [weather] in a town or the shop's own sky. */
    private fun home(hour: Int = 20, weather: Weather? = Weather.RAIN): Home = runBlocking {
        val app = ApplicationProvider.getApplicationContext<UnwrittenApp>()
        val c = app.container
        DemoSeed.seedIfEmpty(c)
        val s = c.repo.refresh()
        val at = LocalDate.now().atTime(hour, 10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val st = c.settings.now().copy(
            onboarded = true,
            place = weather?.let { Place(if (lang == "ru") "Прага" else "Prague", "", 50.08, 14.42) },
            weather = weather?.let { WeatherNow(it, if (it.snow) -2.0 else 9.0, at - 60_000) },
        )
        Home(s, st, Words(lang == "ru"), at, c.requests)
    }

    private fun nav() = Nav(mutableStateListOf())

    private fun shot(name: String, h: Home = home(), content: @Composable (Home) -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            ShopTheme {
                CompositionLocalProvider(LocalToast provides Toaster()) {
                    Box(Modifier.fillMaxSize().wallpaper(h.sky.daylight)) { content(h) }
                }
            }
        }
        rule.mainClock.advanceTimeBy(2_500)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun first(h: Home) = h.shop.books.first()

    @Test fun welcome() = shot("0-welcome") { Onboarding(it.w, it.settings.copy(onboarded = false)) }
    @Test fun shop() = shot("1-shop") { ShopScreen(it, nav()) }
    @Test fun shopDay() = shot("2-day", home(13, Weather.CLEAR)) { ShopScreen(it, nav()) }
    @Test fun shopSnow() = shot("2-snow", home(17, Weather.SNOW)) { ShopScreen(it, nav()) }
    @Test fun write() = shot("3-write") { h -> ShopScreen(h, nav()); WriteSheet(h, nav(), h.visit) }
    @Test fun cover() = shot("4-cover") { h -> BookSheet(h, nav(), first(h).id, Page.COVER) }
    @Test fun back() = shot("5-back") { h -> BookSheet(h, nav(), first(h).id, Page.BACK) }
    @Test fun open() = shot("6-open") { h -> BookSheet(h, nav(), first(h).id, Page.OPEN) }
    @Test fun card() = shot("7-card") { h -> BookSheet(h, nav(), first(h).id, Page.CARD) }
    @Test fun catalog() = shot("8-catalog") { h -> CatalogSheet(h, nav()) }
    @Test fun settings() = shot("x-settings") { h -> SettingsSheet(h) {} }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}
