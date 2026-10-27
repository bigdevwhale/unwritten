package app.unwritten.ui.art

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.unwritten.domain.Phase
import app.unwritten.domain.SkyNow
import app.unwritten.domain.Weather
import app.unwritten.ui.theme.C
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** The shop's wallpaper: small dots, faint stripes, darker towards the floor; lighter when the day comes in. */
fun Modifier.wallpaper(daylight: Float = 0f) = this.drawBehind {
    drawRect(C.wall)
    var x = 0f
    val stripe = 22 * density
    while (x < size.width) { drawRect(Color(0x09FFE6BE), Offset(x, 0f), Size(stripe, size.height)); x += stripe * 2 }
    val step = 24 * density
    var y = step / 2
    while (y < size.height) {
        var xx = step / 2
        while (xx < size.width) { drawCircle(C.wallDot, 2 * density, Offset(xx, y)); xx += step }
        y += step
    }
    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .45f)), size.height * .55f, size.height))
    if (daylight > 0f) drawRect(Color(0xFFFFEBCD).copy(alpha = .10f * daylight))
}

private fun lerp(a: Color, b: Color, k: Float) = Color(a.red + (b.red - a.red) * k, a.green + (b.green - a.green) * k, a.blue + (b.blue - a.blue) * k, a.alpha + (b.alpha - a.alpha) * k)

/** The colours of the sky, top and bottom. */
fun skyColors(s: SkyNow): Pair<Color, Color> {
    val grey = s.weather.grey
    return when (s.phase) {
        Phase.NIGHT -> Color(0xFF0B1224) to Color(0xFF1F2442)
        Phase.DAWN -> Color(0xFF3E4A7C) to Color(0xFFEAA57E)
        Phase.DUSK -> Color(0xFF2C2E5E) to Color(0xFFDA7B58)
        Phase.DAY -> if (grey) Color(0xFF8E99A6) to Color(0xFFC3CBD3) else Color(0xFF5D9AD6) to Color(0xFFCBE2F3)
    }.let { (a, b) -> if (grey && s.phase != Phase.DAY) lerp(a, Color(0xFF555A66), .35f) to lerp(b, Color(0xFF6A6E78), .45f) else a to b }
}

/**
 * The window onto the street: the real sky (sun or moon where they are now, the moon in its phase), clouds,
 * rooftops with lit windows in the evening, and rain, snow, fog or a storm. [t] is seconds, for the weather's motion.
 */
fun DrawScope.drawWindow(s: SkyNow, t: Float) = clipRect { windowScene(s, t) }

private fun DrawScope.windowScene(s: SkyNow, t: Float) {
    val d = density
    val w = size.width; val h = size.height
    val frame = 10 * d
    val (top, bottom) = skyColors(s)
    drawRect(Brush.verticalGradient(listOf(top, bottom)))
    val grey = s.weather.grey
    val night = s.phase == Phase.NIGHT
    // Stars.
    if (!grey && s.phase != Phase.DAY) {
        val r = Random(11)
        val a = if (night) 1f else .35f
        repeat(40) {
            val x = r.nextFloat() * w; val y = r.nextFloat() * h * .6f
            val tw = .55f + .45f * sin(t * (.6f + r.nextFloat()) + it).toFloat()
            drawCircle(Color.White.copy(alpha = a * tw * (.4f + r.nextFloat() * .6f)), (.6f + r.nextFloat() * .7f) * d, Offset(x, y))
        }
    }
    fun arc(f: Float): Offset = Offset(frame + 14 * d + f * (w - 2 * frame - 28 * d), h * .62f - sin(f * PI).toFloat() * h * .46f)
    // The sun.
    s.sun?.let { f ->
        val p = arc(f)
        val a = if (grey) .25f else 1f
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFDC78).copy(alpha = .55f * a), Color.Transparent), p, 34 * d), 34 * d, p)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFBE6), Color(0xFFFFD76A), Color(0xFFFFB347)), p, 11 * d).let { it }, 11 * d * (if (grey) .9f else 1f), p, alpha = a)
    }
    // The moon, lit as it is tonight.
    s.moon?.let { f ->
        val ph = s.moonPhase
        if (!grey && abs(ph - .5) < .47) {
            val p = arc(f)
            val r = 10 * d
            drawCircle(Brush.radialGradient(listOf(Color(0x40E6E6C8), Color.Transparent), p, 26 * d), 26 * d, p)
            drawCircle(Color(0xFFF3ECD4), r, p)
            val off = (if (ph < .5) -ph * 4 * r else (1 - ph) * 4 * r).toFloat()
            clipRect(p.x - r, p.y - r, p.x + r, p.y + r) {
                drawCircle(top.copy(alpha = .96f), r * 1.02f, Offset(p.x + off, p.y))
            }
        }
    }
    // Clouds drifting.
    if (s.weather != Weather.CLEAR) {
        val cloud = when (s.phase) {
            Phase.NIGHT -> Color(0xFF2C3247); Phase.DAWN -> Color(0xFFC99A9A); Phase.DUSK -> Color(0xFF8A5A6A)
            Phase.DAY -> if (grey) Color(0xFFA9B1BA) else Color(0xFFF4F7FA)
        }
        if (grey) drawRect(Brush.verticalGradient(listOf(cloud.copy(alpha = .9f), cloud.copy(alpha = 0f)), 0f, h * .85f))
        val period = w + 160 * d
        val r = Random(5)
        repeat(if (grey) 7 else 4) { i ->
            val speed = (4f + r.nextFloat() * 4f) * d
            val x = ((r.nextFloat() * period + t * speed) % period) - 80 * d
            val y = (10 + r.nextFloat() * 36) * d
            val k = .7f + r.nextFloat() * .6f
            for ((dx, dy, rw) in listOf(Triple(0f, 0f, 30f), Triple(22f, -6f, 22f), Triple(40f, 2f, 26f))) {
                drawOval(cloud.copy(alpha = if (grey) .95f else .9f), Offset(x + dx * k * d, y + dy * k * d), Size(rw * 2 * k * d, rw * .8f * k * d))
            }
            if (i > 5) return@repeat
        }
    }
    // Rooftops across the street.
    val houses = when (s.phase) {
        Phase.NIGHT -> Color(0xFF141320) to Color(0xFF1B1926)
        Phase.DAWN -> Color(0xFF2A2638) to Color(0xFF33304A)
        Phase.DAY -> Color(0xFF5E5A66) to Color(0xFF6C6774)
        Phase.DUSK -> Color(0xFF24203A) to Color(0xFF2E2944)
    }
    val base = h - 4 * d
    val rr = Random(3)
    var x = 0f
    var i = 0
    while (x < w) {
        val bw = (36 + rr.nextFloat() * 40) * d
        val bh = (26 + rr.nextFloat() * 26) * d
        val c = if (i % 2 == 0) houses.first else houses.second
        drawRect(c, Offset(x, base - bh), Size(bw, bh + 4 * d))
        if (i % 3 == 1) drawPath(Path().apply { moveTo(x, base - bh); lineTo(x + bw / 2, base - bh - 12 * d); lineTo(x + bw, base - bh); close() }, c)
        if (i % 4 == 2) drawRect(c, Offset(x + bw * .7f, base - bh - 14 * d), Size(6 * d, 14 * d))
        // Lit windows in the evening and at night; a few go dark late.
        if (s.phase != Phase.DAY) {
            var wy = base - bh + 7 * d
            var row = 0
            while (wy < base - 8 * d) {
                var wx = x + 6 * d
                var col = 0
                while (wx < x + bw - 8 * d) {
                    val on = Random(i * 97 + row * 13 + col).nextFloat() < (if (night) .45f else .7f)
                    if (on) drawRect(Color(0xFFFFD27A).copy(alpha = .85f), Offset(wx, wy), Size(4 * d, 5 * d))
                    wx += 10 * d; col++
                }
                wy += 11 * d; row++
            }
        }
        x += bw + (if (i % 3 == 0) 6 * d else 0f)
        i++
    }
    // Fog.
    if (s.weather == Weather.FOG || s.weather.snow) {
        val a = if (s.weather == Weather.FOG) .85f else .25f
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFDCDCE1).copy(alpha = a)), h * .2f, h))
    }
    // Rain.
    if (s.weather.rain) {
        val heavy = s.weather == Weather.RAIN || s.weather == Weather.STORM
        val n = if (heavy) 70 else 34
        val r = Random(9)
        val fall = (if (heavy) 340f else 220f) * d
        repeat(n) {
            val x0 = r.nextFloat() * (w + 30 * d)
            val y0 = ((r.nextFloat() * h + t * fall * (.8f + r.nextFloat() * .4f)) % (h + 20 * d)) - 10 * d
            val len = (if (heavy) 11f else 7f) * d
            drawLine(Color(0xFFC8D7FF).copy(alpha = .35f), Offset(x0 - y0 * .18f, y0), Offset(x0 - y0 * .18f - len * .18f, y0 + len), .9f * d, StrokeCap.Round)
        }
    }
    // Snow.
    if (s.weather.snow) {
        val r = Random(13)
        repeat(if (s.weather == Weather.SNOW) 60 else 24) {
            val x0 = r.nextFloat() * w
            val sp = (16 + r.nextFloat() * 18) * d
            val y0 = ((r.nextFloat() * h + t * sp) % (h + 10 * d)) - 5 * d
            val sway = sin(t * .8f + it).toFloat() * 5 * d
            drawCircle(Color.White.copy(alpha = .85f), (1f + r.nextFloat() * 1.2f) * d, Offset(x0 + sway, y0))
        }
    }
    // Lightning, now and then.
    if (s.weather == Weather.STORM) {
        val p = t % 9f
        val a = when { p in 8.4f..8.5f -> .75f; p in 8.6f..8.68f -> .55f; else -> 0f }
        if (a > 0) drawRect(Color(0xFFF4F6FF).copy(alpha = a))
    }
    // Glass and frame.
    drawRect(Brush.linearGradient(listOf(Color.White.copy(alpha = .06f), Color.Transparent, Color.White.copy(alpha = .04f)), Offset.Zero, Offset(w, h)))
    val wood = Color(0xFF4A3220)
    drawRect(wood, style = Stroke(frame * 2))
    drawRect(wood, Offset(w / 2 - 4 * d, 0f), Size(8 * d, h))
    drawRect(wood, Offset(0f, h * .44f), Size(w, 8 * d))
    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = if (s.phase == Phase.DAY) .2f else .45f)), Offset(w / 2, h / 2), w * .7f))
}

/** The lamp over the counter: cord, green shade, bulb and its glow, which softens by day. [t] lets it breathe. */
fun DrawScope.drawLamp(cx: Float, cordTop: Float, cordLen: Float, daylight: Float, t: Float) {
    val d = density
    val by = cordTop + cordLen + 26 * d
    val breath = .92f + .08f * sin(t * 1.2f).toFloat()
    val glow = (1f - .7f * daylight) * breath
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFC46E).copy(alpha = .34f * glow), Color.Transparent), Offset(cx, by), 210 * d), 210 * d, Offset(cx, by))
    drawLine(Color(0xFF1C130C), Offset(cx, cordTop), Offset(cx, cordTop + cordLen), 2 * d)
    val shade = Path().apply {
        moveTo(cx - 14 * d, cordTop + cordLen)
        lineTo(cx + 14 * d, cordTop + cordLen)
        cubicTo(cx + 26 * d, cordTop + cordLen + 4 * d, cx + 38 * d, cordTop + cordLen + 14 * d, cx + 40 * d, by - 2 * d)
        lineTo(cx - 40 * d, by - 2 * d)
        cubicTo(cx - 38 * d, cordTop + cordLen + 14 * d, cx - 26 * d, cordTop + cordLen + 4 * d, cx - 14 * d, cordTop + cordLen)
        close()
    }
    drawPath(shade, Brush.verticalGradient(listOf(Color(0xFF5B7A4E), Color(0xFF3D5534)), cordTop + cordLen, by))
    drawRect(Color(0xFFA88A3A), Offset(cx - 40 * d, by - 4 * d), Size(80 * d, 2.5f * d))
    drawOval(Color(0xFFFFF5D0).copy(alpha = .5f + .5f * glow), Offset(cx - 9 * d, by - 3 * d), Size(18 * d, 10 * d))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE6A6).copy(alpha = .8f * glow), Color.Transparent), Offset(cx, by + 2 * d), 30 * d), 30 * d, Offset(cx, by + 2 * d))
}

/** Dust drifting in the lamplight. */
fun DrawScope.drawMotes(c: Offset, t: Float, daylight: Float) {
    val d = density
    val r = Random(21)
    repeat(16) {
        val x0 = (r.nextFloat() - .5f) * 220 * d
        val y0 = r.nextFloat() * 200 * d
        val period = 9f + r.nextFloat() * 8f
        val p = ((t + r.nextFloat() * period) % period) / period
        val a = sin(p * PI).toFloat() * .75f
        val x = c.x + x0 + sin(p * 2 * PI + it).toFloat() * 10 * d
        val y = c.y + y0 - p * 34 * d
        drawCircle((if (daylight > .5f) Color(0xFFFFFAE6) else Color(0xFFFFE2AA)).copy(alpha = a), 1.4f * d, Offset(x, y))
    }
}

/** Wood with its grain, for shelves, the counter and the crate. */
fun DrawScope.drawWood(base: Color, topLeft: Offset, sz: Size, seed: Int, vertical: Boolean = false) {
    drawRect(base, topLeft, sz)
    val r = Random(seed)
    val d = density
    if (!vertical) {
        var y = topLeft.y
        while (y < topLeft.y + sz.height) {
            val c = if (r.nextBoolean()) Color.Black.copy(alpha = .06f + r.nextFloat() * .05f) else Color.White.copy(alpha = .03f + r.nextFloat() * .03f)
            drawRect(c, Offset(topLeft.x, y), Size(sz.width, (1 + r.nextFloat() * 2) * d))
            y += (3 + r.nextFloat() * 4) * d
        }
    } else {
        var x = topLeft.x
        while (x < topLeft.x + sz.width) {
            drawRect(Color.Black.copy(alpha = .05f + r.nextFloat() * .06f), Offset(x, topLeft.y), Size((1 + r.nextFloat() * 2) * d, sz.height))
            x += (3 + r.nextFloat() * 5) * d
        }
    }
}

/** Shelf ornaments between the books. */
enum class Deco { PLANT, CANDLE, STACK, GLOBE, HOURGLASS }

fun decoWidth(k: Deco) = when (k) { Deco.PLANT -> 38; Deco.CANDLE -> 20; Deco.STACK -> 56; Deco.GLOBE -> 38; Deco.HOURGLASS -> 24 }

fun DrawScope.drawDeco(k: Deco, t: Float) {
    val d = density
    val w = size.width; val h = size.height
    when (k) {
        Deco.PLANT -> {
            val leaf = listOf(-32f to 30f, 6f to 38f, 36f to 32f, -12f to 26f, 20f to 24f)
            for ((a, len) in leaf) rotate(a + sin(t * .5f + a).toFloat() * 1.5f, Offset(w / 2, h - 22 * d)) {
                drawOval(Brush.verticalGradient(listOf(Color(0xFF8FBE6E), Color(0xFF4B7A3C)), h - 22 * d - len * d, h - 22 * d), Offset(w / 2 - 5 * d, h - 22 * d - len * d), Size(10 * d, len * d))
            }
            val pot = Path().apply { moveTo(w / 2 - 14 * d, h - 26 * d); lineTo(w / 2 + 14 * d, h - 26 * d); lineTo(w / 2 + 10 * d, h); lineTo(w / 2 - 10 * d, h); close() }
            drawPath(pot, Brush.verticalGradient(listOf(Color(0xFFC56B46), Color(0xFF9B4B2E)), h - 26 * d, h))
            drawRect(Color(0xFFD97C55), Offset(w / 2 - 15 * d, h - 27 * d), Size(30 * d, 5 * d))
        }
        Deco.CANDLE -> {
            val f = 1f + .1f * sin(t * 7f).toFloat() + .05f * sin(t * 13f).toFloat()
            val top = h - 36 * d
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFB450).copy(alpha = .35f), Color.Transparent), Offset(w / 2, top - 8 * d), 26 * d), 26 * d, Offset(w / 2, top - 8 * d))
            drawRect(Brush.horizontalGradient(listOf(Color(0xFFE2D3AE), Color(0xFFFFF6DF), Color(0xFFE2D3AE)), w / 2 - 6 * d, w / 2 + 6 * d), Offset(w / 2 - 6 * d, top), Size(12 * d, h - top))
            drawOval(Color(0xFFEFE3C4), Offset(w / 2 - 6 * d, top - 2 * d), Size(12 * d, 4 * d))
            drawLine(Color(0xFF3A2A1E), Offset(w / 2, top), Offset(w / 2, top - 3 * d), 1 * d)
            val fh = 13 * d * f
            drawOval(Brush.verticalGradient(listOf(Color(0xFFFFF8D8), Color(0xFFFFC350), Color(0xFFFF8A2A)), top - fh - 2 * d, top), Offset(w / 2 - 4 * d, top - fh - 2 * d), Size(8 * d, fh))
        }
        Deco.STACK -> {
            val cols = listOf(Color(0xFF7A3B2E), Color(0xFF3E5A4A), Color(0xFFC9A55A), Color(0xFF5A4A6E))
            val inset = listOf(0f, 4f, 1f, 6f)
            val ww = listOf(56f, 46f, 52f, 40f)
            for (i in cols.indices) {
                val y = h - (i + 1) * 10 * d
                drawRoundRect(cols[i], Offset(inset[i] * d, y), Size(ww[i] * d, 9.5f * d), CornerRadius(1.5f * d))
                drawRect(Color(0xFFF1E6C8).copy(alpha = .8f), Offset((inset[i] + ww[i] - 3) * d, y + 1.5f * d), Size(2 * d, 6.5f * d))
            }
        }
        Deco.GLOBE -> {
            val c = Offset(w / 2, h - 30 * d)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF9CC7C0), Color(0xFF3F7A72), Color(0xFF264C46)), Offset(c.x - 5 * d, c.y - 5 * d), 18 * d), 14 * d, c)
            val spin = (t * 3f) % 28f
            clipRect(c.x - 14 * d, c.y - 14 * d, c.x + 14 * d, c.y + 14 * d) {
                drawOval(Color(0xFFC9B56A).copy(alpha = .85f), Offset(c.x - 14 * d + spin * d, c.y - 8 * d), Size(10 * d, 7 * d))
                drawOval(Color(0xFFC9B56A).copy(alpha = .85f), Offset(c.x - 24 * d + spin * d, c.y + 2 * d), Size(8 * d, 9 * d))
            }
            drawArc(Color(0xFFB8904C), 200f, 260f, false, Offset(c.x - 17 * d, c.y - 17 * d), Size(34 * d, 34 * d), style = Stroke(1.6f * d))
            drawPath(Path().apply { moveTo(c.x - 3 * d, c.y + 14 * d); lineTo(c.x + 3 * d, c.y + 14 * d); lineTo(c.x + 9 * d, h); lineTo(c.x - 9 * d, h); close() }, Color(0xFF8C6A34))
        }
        Deco.HOURGLASS -> {
            val c = w / 2
            drawRect(Color(0xFF6E4A2C), Offset(c - 10 * d, h - 4 * d), Size(20 * d, 4 * d))
            drawRect(Color(0xFF6E4A2C), Offset(c - 10 * d, h - 44 * d), Size(20 * d, 4 * d))
            val glass = Path().apply { moveTo(c - 8 * d, h - 40 * d); lineTo(c + 8 * d, h - 40 * d); lineTo(c + 1.5f * d, h - 24 * d); lineTo(c + 8 * d, h - 4 * d); lineTo(c - 8 * d, h - 4 * d); lineTo(c - 1.5f * d, h - 24 * d); close() }
            drawPath(glass, Color(0x55DDE8F0))
            val sand = ((t / 60f) % 1f)
            drawPath(Path().apply { moveTo(c - 8 * d * (1 - sand), h - 34 * d + sand * 8 * d); lineTo(c + 8 * d * (1 - sand), h - 34 * d + sand * 8 * d); lineTo(c, h - 25 * d); close() }, Color(0xFFE2C47A))
            drawPath(Path().apply { moveTo(c - 8 * d, h - 4 * d); lineTo(c + 8 * d, h - 4 * d); lineTo(c, h - 4 * d - (4 + sand * 10) * d); close() }, Color(0xFFE2C47A))
            drawLine(Color(0xFFE2C47A), Offset(c, h - 25 * d), Offset(c, h - 6 * d), .8f * d)
        }
    }
}

/** A cup of tea on the counter, steam curling up. */
fun DrawScope.drawCup(t: Float) {
    val d = density
    val w = size.width; val h = size.height
    val top = h - 26 * d
    for (i in 0 until 2) {
        val p = ((t * .35f + i * .5f) % 1f)
        val x = w / 2 - 6 * d + i * 10 * d
        val path = Path().apply {
            moveTo(x, top - 2 * d)
            cubicTo(x - 5 * d, top - 8 * d - p * 8 * d, x + 5 * d, top - 14 * d - p * 10 * d, x, top - 22 * d - p * 12 * d)
        }
        drawPath(path, Color.White.copy(alpha = .35f * sin(p * PI).toFloat()), style = Stroke(1.6f * d, cap = StrokeCap.Round))
    }
    drawOval(Color(0xFFE2D7C2), Offset(w / 2 - 22 * d, h - 6 * d), Size(44 * d, 6 * d))
    val cup = Path().apply {
        moveTo(w / 2 - 16 * d, top); lineTo(w / 2 + 16 * d, top)
        cubicTo(w / 2 + 16 * d, h - 10 * d, w / 2 + 10 * d, h - 4 * d, w / 2, h - 4 * d)
        cubicTo(w / 2 - 10 * d, h - 4 * d, w / 2 - 16 * d, h - 10 * d, w / 2 - 16 * d, top); close()
    }
    drawPath(cup, Brush.horizontalGradient(listOf(Color(0xFFDDD2BD), Color(0xFFFFFAF0), Color(0xFFDDD2BD)), w / 2 - 16 * d, w / 2 + 16 * d))
    drawArc(Color(0xFFE9E0CF), -80f, 160f, false, Offset(w / 2 + 11 * d, top + 3 * d), Size(12 * d, 12 * d), style = Stroke(3 * d))
    drawOval(Color(0xFF8A4A22), Offset(w / 2 - 14 * d, top - 1.5f * d), Size(28 * d, 4 * d))
}

/** The brass bell on the counter; [ring] 0…1 swings it. */
fun DrawScope.drawBell(ring: Float) {
    val d = density
    val w = size.width; val h = size.height
    val swing = sin(ring * PI * 3).toFloat() * 14f * (1 - ring)
    drawRect(Color(0xFF5A4020), Offset(2 * d, h - 5 * d), Size(w - 4 * d, 5 * d))
    rotate(swing, Offset(w / 2, h - 5 * d)) {
        drawArc(Brush.radialGradient(listOf(Color(0xFFFFF2B8), Color(0xFFD9A840), Color(0xFF8F6A20)), Offset(w * .38f, h * .45f), w * .6f), 180f, 180f, true, Offset(5 * d, h - 5 * d - (h - 10 * d)), Size(w - 10 * d, (h - 10 * d) * 2))
        drawCircle(Color(0xFFD9A840), 3 * d, Offset(w / 2, h - (h - 10 * d) - 6 * d))
    }
}
