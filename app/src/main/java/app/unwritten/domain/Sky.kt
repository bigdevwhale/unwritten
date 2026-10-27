package app.unwritten.domain

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/** What's outside the shop window. */
enum class Weather { CLEAR, CLOUDY, OVERCAST, FOG, DRIZZLE, RAIN, STORM, SNOW, SLEET;

    val rain get() = this == DRIZZLE || this == RAIN || this == STORM || this == SLEET
    val snow get() = this == SNOW || this == SLEET
    val grey get() = this != CLEAR && this != CLOUDY

    companion object {
        /** The WMO weather code Open-Meteo reports. */
        fun wmo(code: Int): Weather = when (code) {
            0, 1 -> CLEAR
            2 -> CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 80, 81, 82 -> RAIN
            66, 67 -> SLEET
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> STORM
            else -> CLOUDY
        }

        fun of(s: String?) = entries.firstOrNull { it.name == s }
    }
}

/** The weather as last fetched. */
data class WeatherNow(val weather: Weather, val temp: Double, val at: Long)

/** The town whose sky the window shows. */
data class Place(val name: String, val region: String, val lat: Double, val lon: Double)

/** Morning, day, evening or night: what the light in the shop follows. */
enum class Phase { NIGHT, DAWN, DAY, DUSK }

/**
 * Everything the window needs: the light, the weather, where the sun and the moon are (0…1 across the
 * window, or null below the horizon), and the moon's phase (0 new, .5 full).
 */
data class SkyNow(val phase: Phase, val weather: Weather, val sun: Float?, val moon: Float?, val moonPhase: Double, val temp: Double?) {
    /** How much daylight comes through the window into the shop, 0…1. */
    val daylight: Float get() = when (phase) {
        Phase.DAY -> if (weather.grey) .6f else 1f
        Phase.DAWN, Phase.DUSK -> .35f
        Phase.NIGHT -> 0f
    }
}

object Sky {
    /** Real weather is kept for three hours; older than that the window falls back to plausible weather of its own. */
    const val FRESH_MS = 3 * 60 * 60 * 1000L
    /** Fetched again after half an hour. */
    const val STALE_MS = 30 * 60 * 1000L
    private const val TWILIGHT = 40

    fun now(at: ZonedDateTime, place: Place?, real: WeatherNow?): SkyNow {
        val date = at.toLocalDate()
        val sun = place?.let { Sun.times(date, it.lat, it.lon, at.zone) } ?: Sun.fallback(date, at.zone)
        val m = at.hour * 60 + at.minute
        val phase = when {
            m < sun.rise - TWILIGHT || m > sun.set + TWILIGHT -> Phase.NIGHT
            m < sun.rise + TWILIGHT -> Phase.DAWN
            m > sun.set - TWILIGHT -> Phase.DUSK
            else -> Phase.DAY
        }
        val fresh = real?.takeIf { at.toInstant().toEpochMilli() - it.at < FRESH_MS }
        val w = fresh ?: simulated(at.toLocalDateTime())
        val sunF = if (m in sun.rise - 15..sun.set + 15) ((m - sun.rise).toFloat() / (sun.set - sun.rise)).coerceIn(0f, 1f) else null
        val nightLen = sun.rise + 1440 - sun.set
        val moonF = if (sunF == null) (((if (m > sun.set) m - sun.set else m + 1440 - sun.set).toFloat()) / nightLen).coerceIn(0f, 1f) else null
        return SkyNow(phase, w.weather, sunF, moonF, Moon.phase(at.toInstant().toEpochMilli()), fresh?.temp)
    }

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 6000
        c.readTimeout = 8000
        c.setRequestProperty("User-Agent", "Unwritten/1.0 github.com/bigdevwhale/unwritten")
        try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    /**
     * The weather now at a place: from Open-Meteo, or from MET Norway when Open-Meteo can't be reached
     * (some networks block it). No key; nothing but the coordinates is sent.
     */
    fun fetch(lat: Double, lon: Double, now: Long = System.currentTimeMillis()): WeatherNow =
        runCatching { openMeteo(lat, lon, now) }.getOrElse { metNo(lat, lon, now) }

    private fun openMeteo(lat: Double, lon: Double, now: Long): WeatherNow {
        val j = JSONObject(get("https://api.open-meteo.com/v1/forecast?latitude=%.3f&longitude=%.3f&current=temperature_2m,weather_code".format(Locale.US, lat, lon)))
        val cur = j.getJSONObject("current")
        return WeatherNow(Weather.wmo(cur.getInt("weather_code")), cur.getDouble("temperature_2m"), now)
    }

    private fun metNo(lat: Double, lon: Double, now: Long): WeatherNow {
        val j = JSONObject(get("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%.3f&lon=%.3f".format(Locale.US, lat, lon)))
        val first = j.getJSONObject("properties").getJSONArray("timeseries").getJSONObject(0).getJSONObject("data")
        val d = first.getJSONObject("instant").getJSONObject("details")
        val symbol = first.optJSONObject("next_1_hours")?.optJSONObject("summary")?.optString("symbol_code")
            ?: first.optJSONObject("next_6_hours")?.optJSONObject("summary")?.optString("symbol_code") ?: "cloudy"
        return WeatherNow(symbol(symbol), d.getDouble("air_temperature"), now)
    }

    /** MET Norway's symbol codes ("lightrainshowers_day", "fog"…). */
    fun symbol(code: String): Weather {
        val c = code.substringBefore('_')
        return when {
            "thunder" in c -> Weather.STORM
            "sleet" in c -> Weather.SLEET
            "snow" in c -> Weather.SNOW
            c.startsWith("light") && "rain" in c -> Weather.DRIZZLE
            "rain" in c -> Weather.RAIN
            c == "fog" -> Weather.FOG
            c == "cloudy" -> Weather.OVERCAST
            c == "partlycloudy" -> Weather.CLOUDY
            else -> Weather.CLEAR
        }
    }

    /** Towns matching a name, for the settings search. */
    fun search(name: String, lang: String): List<Place> {
        val q = URLEncoder.encode(name.trim(), "UTF-8")
        val j = JSONObject(get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=8&language=$lang&format=json"))
        val r = j.optJSONArray("results") ?: return emptyList()
        return (0 until r.length()).map { i ->
            val o = r.getJSONObject(i)
            Place(
                o.getString("name"),
                listOf(o.optString("admin1"), o.optString("country")).filter { it.isNotBlank() }.joinToString(", "),
                o.getDouble("latitude"), o.getDouble("longitude"),
            )
        }
    }

    /**
     * The window's own weather when there is no real one: plausible for the month, the same for everyone at the
     * same hour, changing every three hours.
     */
    fun simulated(at: LocalDateTime): WeatherNow {
        val month = at.monthValue
        val slot = at.toLocalDate().toEpochDay().toInt() * 8 + at.hour / 3
        val r = unit(slot, 77)
        val day = unit(at.toLocalDate().toEpochDay().toInt(), 13)
        val winter = month in listOf(12, 1, 2)
        val autumn = month in listOf(9, 10, 11)
        val w = when {
            winter && r < .28 -> Weather.SNOW
            winter && r < .55 -> Weather.OVERCAST
            winter && r < .75 -> Weather.CLOUDY
            winter -> Weather.CLEAR
            month in listOf(3, 11) && r < .12 -> Weather.SLEET
            autumn && r < .3 -> Weather.RAIN
            autumn && r < .42 -> Weather.FOG
            month in listOf(6, 7, 8) && r < .08 -> Weather.STORM
            r < .2 -> Weather.RAIN
            r < .4 -> Weather.OVERCAST
            r < .65 -> Weather.CLOUDY
            else -> Weather.CLEAR
        }
        val base = doubleArrayOf(-9.0, -8.0, -2.0, 6.0, 13.0, 17.0, 19.0, 17.0, 11.0, 5.0, -1.0, -6.0)[month - 1]
        return WeatherNow(w, base - 3 * cos((at.hour - 3) / 24.0 * 2 * Math.PI) + (day - .5) * 8, 0)
    }

    private fun unit(i: Int, j: Int): Double {
        var h = i * 374761393 + j * 668265263
        h = (h xor (h ushr 13)) * 1274126177
        return ((h xor (h ushr 16)) and 0xffffff) / 16777216.0
    }
}

/** Sunrise and sunset in minutes after local midnight. */
data class SunTimes(val rise: Int, val set: Int)

object Sun {
    private fun rad(d: Double) = Math.toRadians(d)

    /** The NOAA approximation, good to a couple of minutes; null on polar day or night. */
    fun times(date: LocalDate, lat: Double, lon: Double, zone: ZoneId): SunTimes? {
        val g = 2 * Math.PI / 365 * (date.dayOfYear - 1)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(g) - 0.032077 * sin(g) - 0.014615 * cos(2 * g) - 0.040849 * sin(2 * g))
        val decl = 0.006918 - 0.399912 * cos(g) + 0.070257 * sin(g) - 0.006758 * cos(2 * g) + 0.000907 * sin(2 * g) -
            0.002697 * cos(3 * g) + 0.00148 * sin(3 * g)
        val x = cos(rad(90.833)) / (cos(rad(lat)) * cos(decl)) - tan(rad(lat)) * tan(decl)
        if (x > 1 || x < -1) return null
        val ha = Math.toDegrees(acos(x))
        val offset = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 60.0
        val noon = 720 - 4 * lon - eqTime + offset
        return SunTimes((noon - ha * 4).roundToInt(), (noon + ha * 4).roundToInt())
    }

    /** Typical times at a mid-latitude town in this time zone, when no town is chosen (or the sun never sets). */
    fun fallback(date: LocalDate, zone: ZoneId): SunTimes {
        val offsetH = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
        return times(date, 52.0, offsetH * 15, zone) ?: SunTimes(7 * 60, 19 * 60)
    }
}

object Moon {
    private const val SYNODIC = 29.530588853
    /** A new moon: 6 January 2000, 18:14 UTC. */
    private const val EPOCH = 947182440000L

    /** 0 new, .5 full, back to 1. */
    fun phase(at: Long): Double {
        val days = (at - EPOCH) / 86_400_000.0
        return ((days % SYNODIC) + SYNODIC) % SYNODIC / SYNODIC
    }
}
