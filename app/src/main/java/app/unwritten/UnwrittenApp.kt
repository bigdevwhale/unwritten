package app.unwritten

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.unwritten.data.DemoSeed
import app.unwritten.data.Repo
import app.unwritten.data.SettingsStore
import app.unwritten.data.ShopDb
import app.unwritten.domain.Requests
import app.unwritten.domain.Sky
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val db = ShopDb.create(app)
    val repo = Repo(db)
    val requests: Requests by lazy { Requests.load { runCatching { app.assets.open("requests.tsv") }.getOrNull() } }
    val settings = SettingsStore(app)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val weatherLock = Mutex()

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    /** Fetches the weather for the chosen town when the last reading is older than half an hour. */
    fun refreshWeather(force: Boolean = false) = scope.launch(Dispatchers.IO) {
        if (BuildConfig.DEMO) return@launch
        weatherLock.withLock {
            val s = settings.now()
            val p = s.place ?: return@withLock
            if (!force && s.weather != null && System.currentTimeMillis() - s.weather.at < Sky.STALE_MS) return@withLock
            val w = runCatching { Sky.fetch(p.lat, p.lon) }.getOrNull() ?: return@withLock
            settings.update { it.copy(weather = w) }
        }
    }
}

class UnwrittenApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // The demo shows nothing until its history is in place, so the first screen doesn't jump.
        if (BuildConfig.DEMO) container.repo.hold = true
        container.scope.launch {
            container.requests
            if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(container)
            container.repo.hold = false
            container.repo.refresh()
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as UnwrittenApp).container

/** Resources in the in-app language, which AppCompat doesn't localise outside activities before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

/** Is this context in Russian? */
val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
