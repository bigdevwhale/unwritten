package app.unwritten.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.unwritten.domain.Place
import app.unwritten.domain.Weather
import app.unwritten.domain.WeatherNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** The shop's own imprint, on every cover; empty for the default name. */
    val imprint: String = "",
    /** The town whose sky is in the window; null for a sky of the shop's own. */
    val place: Place? = null,
    val weather: WeatherNow? = null,
    /** The day the bell was last rung, and how many times that day. */
    val ringDay: Long = 0,
    val rings: Int = 0,
    /** The day a returned book was last looked at, so it doesn't come back twice in a day. */
    val returnSeen: Long = 0,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        imprint = this[IMPRINT] ?: "",
        place = this[PLACE]?.takeIf { it.isNotEmpty() }?.let { Place(it, this[REGION] ?: "", this[LAT] ?: 0.0, this[LON] ?: 0.0) },
        weather = Weather.of(this[WEATHER])?.let { WeatherNow(it, this[TEMP] ?: 0.0, this[WEATHER_AT] ?: 0) },
        ringDay = this[RING_DAY] ?: 0,
        rings = this[RINGS] ?: 0,
        returnSeen = this[RETURN_SEEN] ?: 0,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[IMPRINT] = s.imprint
            it[PLACE] = s.place?.name ?: ""
            it[REGION] = s.place?.region ?: ""
            it[LAT] = s.place?.lat ?: 0.0
            it[LON] = s.place?.lon ?: 0.0
            it[WEATHER] = s.weather?.weather?.name ?: ""
            it[TEMP] = s.weather?.temp ?: 0.0
            it[WEATHER_AT] = s.weather?.at ?: 0
            it[RING_DAY] = s.ringDay
            it[RINGS] = s.rings
            it[RETURN_SEEN] = s.returnSeen
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val IMPRINT = stringPreferencesKey("imprint")
        val PLACE = stringPreferencesKey("place")
        val REGION = stringPreferencesKey("region")
        val LAT = doublePreferencesKey("lat")
        val LON = doublePreferencesKey("lon")
        val WEATHER = stringPreferencesKey("weather")
        val TEMP = doublePreferencesKey("temp")
        val WEATHER_AT = longPreferencesKey("weather_at")
        val RING_DAY = longPreferencesKey("ring_day")
        val RINGS = intPreferencesKey("rings")
        val RETURN_SEEN = longPreferencesKey("return_seen")
    }
}
