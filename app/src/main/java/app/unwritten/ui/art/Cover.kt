package app.unwritten.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import app.unwritten.domain.Book
import app.unwritten.domain.Kind
import app.unwritten.domain.Style
import app.unwritten.domain.hash
import app.unwritten.ui.Words
import app.unwritten.ui.theme.Classic
import app.unwritten.ui.theme.Grotesk
import app.unwritten.ui.theme.Narrow
import app.unwritten.ui.theme.Serif
import app.unwritten.ui.theme.Typewriter
import kotlin.random.Random

/** What a cover needs: the book's face, without the rest of it. */
data class Face(val title: String, val author: String, val kind: Kind, val style: Style, val edition: Int = 1) {
    constructor(b: Book) : this(b.title, b.author, b.kind, b.style, b.edition)

    /** The hue every colour of this book is made from; the title decides it, so a book keeps its colour. */
    val hue: Float get() = (hash(title.ifBlank { "?" }) % 360).toFloat()
    val seed: Int get() = hash(title + "y")
}

private fun hsl(h: Float, s: Float, l: Float) = Color.hsl(((h % 360) + 360) % 360, s, l)

/** The spine's cloth and lettering. */
fun spineColors(f: Face): Pair<Color, Color> = when (f.style) {
    Style.SERIES -> hsl(f.hue, .32f, .30f) to hsl(f.hue, .45f, .84f)
    Style.TRICOLOUR -> hsl(f.hue, .72f, .48f) to Color.White
    Style.PULP -> hsl(f.hue, .80f, .38f) to Color(0xFFFFE14D)
    Style.SAMIZDAT -> Color(0xFFE2DCCB) to Color(0xFF222222)
}

fun fontOf(s: Style): FontFamily = when (s) {
    Style.SERIES -> Classic
    Style.TRICOLOUR -> Grotesk
    Style.PULP -> Narrow
    Style.SAMIZDAT -> Typewriter
}

/** A cover at 2:3. */
@Composable
fun Cover(f: Face, w: Words, imprint: String, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer(cacheSize = 32)
    Canvas(modifier.aspectRatio(2f / 3f)) { drawCover(f, w, imprint, tm) }
}

/**
 * Draws a cover filling the scope (2:3) in design units of 200×300. Shared by the screen and the printed zine,
 * so a book looks the same in the hand and on paper.
 */
fun DrawScope.drawCover(f: Face, w: Words, imprint: String, tm: TextMeasurer) {
    val u = size.width / 200f
    val title = f.title.ifBlank { w.t("Без названия", "Untitled") }
    val author = f.author.ifBlank { w.t("Автор неизвестен", "Author unknown") }
    val r = CornerRadius(4 * u)
    when (f.style) {
        Style.SERIES -> {
            val bg = hsl(f.hue, .32f, .30f); val fg = hsl(f.hue, .45f, .84f)
            drawRoundRect(bg, cornerRadius = r)
            cloth(u, Random(f.seed))
            drawRect(fg, Offset(10 * u, 10 * u), Size(180 * u, 280 * u), style = Stroke(2.2f * u))
            drawRect(fg.copy(alpha = .7f), Offset(15 * u, 15 * u), Size(170 * u, 270 * u), style = Stroke(.8f * u))
            val series = w.series[f.seed % w.series.size]
            text(tm, series.uppercase(), TextStyle(fontFamily = Classic, fontSize = (7.5f * u).sp(this), letterSpacing = (1.6f * u).sp(this), color = fg.copy(alpha = .85f), textAlign = TextAlign.Center), 22 * u, 28 * u, 156 * u, 2)
            text(tm, author, TextStyle(fontFamily = Classic, fontSize = (11.5f * u).sp(this), color = fg, textAlign = TextAlign.Center), 22 * u, 66 * u, 156 * u, 2)
            text(tm, title, TextStyle(fontFamily = Classic, fontSize = (fit(title, 22f, 15f)) .times(u).sp(this), lineHeight = (fit(title, 25f, 18f) * u).sp(this), color = fg, textAlign = TextAlign.Center), 22 * u, 92 * u, 156 * u, 5)
            ornament(f.seed, Offset(100 * u, 232 * u), u, fg)
            val year = 1951 + f.seed % 40
            text(tm, "${w.imprint(imprint)} · $year", TextStyle(fontFamily = Classic, fontSize = (7f * u).sp(this), letterSpacing = (.6f * u).sp(this), color = fg.copy(alpha = .8f), textAlign = TextAlign.Center), 22 * u, 256 * u, 156 * u, 1)
        }
        Style.TRICOLOUR -> {
            val band = hsl(f.hue, .72f, .48f); val cream = Color(0xFFF7F2E7)
            drawRoundRect(band, cornerRadius = r)
            drawRect(cream, Offset(0f, 78 * u), Size(size.width, 140 * u))
            drawLine(band, Offset(16 * u, 86 * u), Offset(184 * u, 86 * u), u)
            drawLine(band, Offset(16 * u, 210 * u), Offset(184 * u, 210 * u), u)
            text(tm, w.kind(f.kind).uppercase(), TextStyle(fontFamily = Grotesk, fontWeight = FontWeight.Bold, fontSize = (8f * u).sp(this), letterSpacing = (2.4f * u).sp(this), color = Color.White, textAlign = TextAlign.Center), 10 * u, 34 * u, 180 * u, 1)
            val t = measure(tm, title, TextStyle(fontFamily = Grotesk, fontWeight = FontWeight.Bold, fontSize = (fit(title, 19f, 14f) * u).sp(this), lineHeight = (fit(title, 22f, 17f) * u).sp(this), color = Color(0xFF1D1D1D), textAlign = TextAlign.Center), 160 * u, 4)
            val a = measure(tm, author, TextStyle(fontFamily = Grotesk, fontWeight = FontWeight.Bold, fontSize = (9.5f * u).sp(this), color = Color(0xFF444444), textAlign = TextAlign.Center), 160 * u, 1)
            val top = 148 * u - (t.size.height + 8 * u + a.size.height) / 2f
            drawText(t, topLeft = Offset(20 * u, top))
            drawText(a, topLeft = Offset(20 * u, top + t.size.height + 8 * u))
            drawOval(cream, Offset(78 * u, 236 * u), Size(44 * u, 30 * u))
            nib(Offset(100 * u, 251 * u), u, band.copy(alpha = 1f).darker())
        }
        Style.PULP -> {
            val a = hsl(f.hue, .85f, .48f); val b = hsl(f.hue + 50, .80f, .18f)
            drawRoundRect(Brush.linearGradient(listOf(a, b), Offset(0f, 0f), Offset(size.width * .6f, size.height)), cornerRadius = r)
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = .35f), Color.Black.copy(alpha = .4f)), Offset(84 * u, 112 * u), 70 * u), 60 * u, Offset(100 * u, 132 * u))
            halftone(u)
            text(tm, author.uppercase(), TextStyle(fontFamily = Narrow, fontWeight = FontWeight.Bold, fontSize = (10.5f * u).sp(this), letterSpacing = (1.5f * u).sp(this), color = Color.White), 14 * u, 14 * u, 120 * u, 1)
            rotate(12f, Offset(158 * u, 62 * u)) {
                drawCircle(Color(0xFFFFE14D), 23 * u, Offset(158 * u, 62 * u))
                text(tm, w.t("10\nкоп.", "10¢\nonly"), TextStyle(fontFamily = Narrow, fontWeight = FontWeight.Bold, fontSize = (10f * u).sp(this), lineHeight = (10f * u).sp(this), color = Color(0xFF111111), textAlign = TextAlign.Center), 135 * u, 51 * u, 46 * u, 2)
            }
            val ts = TextStyle(fontFamily = Narrow, fontWeight = FontWeight.Bold, fontSize = (fit(title, 32f, 22f) * u).sp(this), lineHeight = (fit(title, 30f, 21f) * u).sp(this), textAlign = TextAlign.Start)
            val shadow = measure(tm, title.uppercase(), ts.copy(color = Color.Black), 172 * u, 4)
            val lit = measure(tm, title.uppercase(), ts.copy(color = Color(0xFFFFE14D)), 172 * u, 4)
            val y = 240 * u - lit.size.height
            rotate(-6f, Offset(100 * u, y + lit.size.height / 2f)) {
                drawText(shadow, topLeft = Offset(15.5f * u, y + 2.5f * u))
                drawText(lit, topLeft = Offset(13 * u, y))
            }
        }
        Style.SAMIZDAT -> {
            drawRoundRect(Color(0xFFE8E2D3), cornerRadius = r)
            val rnd = Random(f.seed)
            repeat(14) { drawCircle(Color(0xFF786440).copy(alpha = .05f + rnd.nextFloat() * .05f), (6 + rnd.nextFloat() * 22) * u, Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)) }
            for (y in listOf(60f, 220f)) drawRect(Color(0xFF9A9A9A), Offset(8 * u, y * u), Size(3 * u, 22 * u))
            text(tm, author, TextStyle(fontFamily = Typewriter, fontSize = (10.5f * u).sp(this), color = Color(0xFF222222)), 26 * u, 26 * u, 160 * u, 2)
            val ts = TextStyle(fontFamily = Typewriter, fontSize = (fit(title, 16.5f, 12.5f) * u).sp(this), lineHeight = (fit(title, 21f, 16f) * u).sp(this), color = Color(0xFF1A1A1A), textDecoration = TextDecoration.Underline)
            // Typed twice, a hair apart: the heavy strike of a tired ribbon.
            text(tm, title, ts, 26 * u, 84 * u, 154 * u, 6)
            text(tm, title, ts.copy(color = Color(0x88222222)), 26.6f * u, 84.3f * u, 154 * u, 6)
            text(tm, w.kind(f.kind).lowercase(), TextStyle(fontFamily = Typewriter, fontSize = (8.5f * u).sp(this), color = Color(0xAA222222)), 26 * u, 270 * u, 100 * u, 1)
            text(tm, w.t("экз. ${1 + f.seed % 5} из 5", "copy ${1 + f.seed % 5} of 5"), TextStyle(fontFamily = Typewriter, fontSize = (8.5f * u).sp(this), color = Color(0xAA222222), textAlign = TextAlign.End), 100 * u, 270 * u, 84 * u, 1)
        }
    }
    if (f.edition > 1) {
        rotate(-8f, Offset(162 * u, 20 * u)) {
            drawRect(Color(0xFFF4EAD0), Offset(132 * u, 12 * u), Size(60 * u, 16 * u))
            text(tm, w.t("${f.edition}-е изд.", "ed. ${f.edition}"), TextStyle(fontFamily = Serif, fontSize = (8.5f * u).sp(this), color = Color(0xFF7A2E1C), textAlign = TextAlign.Center), 132 * u, 13.5f * u, 60 * u, 1)
        }
    }
    // The fold by the spine.
    drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .22f), Color.Transparent), 0f, 7 * u), size = Size(7 * u, size.height))
}

/** Draws a spine filling the scope: cloth, two bands, the title read bottom to top. */
fun DrawScope.drawSpine(f: Face, tm: TextMeasurer) {
    val (bg, fg) = spineColors(f)
    drawRect(bg)
    drawRect(Brush.horizontalGradient(listOf(Color.White.copy(alpha = .14f), Color.Transparent, Color.Black.copy(alpha = .32f))))
    val band = (size.width * .06f).coerceAtLeast(1.5f)
    drawRect(fg.copy(alpha = .55f), Offset(size.width * .12f, size.height * .07f), Size(size.width * .76f, band))
    drawRect(fg.copy(alpha = .55f), Offset(size.width * .12f, size.height * .93f - band), Size(size.width * .76f, band))
    val fs = (size.width * .42f).coerceAtMost(size.height / 9f)
    val l = tm.measure(
        f.title, TextStyle(fontFamily = fontOf(f.style), fontWeight = FontWeight.Bold, fontSize = fs.sp(this), color = fg),
        maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, constraints = Constraints(maxWidth = (size.height * .78f).toInt().coerceAtLeast(1)),
    )
    rotate(-90f, center) {
        drawText(l, topLeft = Offset(center.x - l.size.width / 2f, center.y - l.size.height / 2f))
    }
}

private fun Float.sp(d: DrawScope) = with(d) { this@sp.toSp() }

private fun Color.darker() = Color(red * .6f, green * .6f, blue * .6f, alpha)

/** Long titles get a smaller face. */
private fun fit(title: String, big: Float, small: Float): Float {
    val k = ((title.length - 14) / 40f).coerceIn(0f, 1f)
    return big + (small - big) * k
}

private fun measure(tm: TextMeasurer, s: String, style: TextStyle, width: Float, lines: Int): TextLayoutResult =
    tm.measure(s, style, overflow = TextOverflow.Ellipsis, maxLines = lines, constraints = Constraints(maxWidth = width.toInt().coerceAtLeast(1)))

private fun DrawScope.text(tm: TextMeasurer, s: String, style: TextStyle, x: Float, y: Float, width: Float, lines: Int) {
    val l = measure(tm, s, style, width, lines)
    val dx = when (style.textAlign) { TextAlign.Center -> (width - l.size.width) / 2f; TextAlign.End -> width - l.size.width; else -> 0f }
    drawText(l, topLeft = Offset(x + dx, y))
}

/** The weave of bookcloth: faint diagonal threads. */
private fun DrawScope.cloth(u: Float, r: Random) = clipRect {
    var x = -size.height
    while (x < size.width) {
        drawLine(Color.Black.copy(alpha = .05f + r.nextFloat() * .03f), Offset(x, 0f), Offset(x + size.height, size.height), .6f * u)
        x += 3.2f * u
    }
}

private fun DrawScope.halftone(u: Float) {
    val step = 6 * u
    var y = 0f
    while (y < size.height) {
        var x = if ((y / step).toInt() % 2 == 0) 0f else step / 2
        while (x < size.width) { drawCircle(Color.Black.copy(alpha = .22f), 1.3f * u, Offset(x, y)); x += step }
        y += step
    }
}

/** A small device under the title: a star, a sprig, a lamp or a ship, chosen by the book. */
private fun DrawScope.ornament(seed: Int, c: Offset, u: Float, color: Color) {
    translate(c.x, c.y) {
        when (seed % 4) {
            0 -> {
                val p = Path()
                for (i in 0 until 10) {
                    val a = Math.PI * i / 5 - Math.PI / 2
                    val rr = (if (i % 2 == 0) 11f else 4.5f) * u
                    val x = (kotlin.math.cos(a) * rr).toFloat(); val y = (kotlin.math.sin(a) * rr).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                p.close(); drawPath(p, color)
            }
            1 -> {
                drawLine(color, Offset(0f, 12 * u), Offset(0f, -12 * u), 1.4f * u)
                for (i in 0 until 4) {
                    val y = (8 - i * 5) * u
                    drawOval(color, Offset(1 * u, y - 4 * u), Size(9 * u, 4 * u))
                    drawOval(color, Offset(-10 * u, y - 2 * u), Size(9 * u, 4 * u))
                }
            }
            2 -> {
                drawArc(color, 180f, 180f, true, Offset(-11 * u, -10 * u), Size(22 * u, 16 * u))
                drawRect(color, Offset(-1 * u, -2 * u), Size(2 * u, 12 * u))
                drawRect(color, Offset(-6 * u, 10 * u), Size(12 * u, 2 * u))
            }
            else -> {
                val hull = Path().apply { moveTo(-13 * u, 4 * u); lineTo(13 * u, 4 * u); lineTo(9 * u, 10 * u); lineTo(-9 * u, 10 * u); close() }
                drawPath(hull, color)
                drawLine(color, Offset(0f, 4 * u), Offset(0f, -13 * u), 1.4f * u)
                drawPath(Path().apply { moveTo(1.5f * u, -12 * u); lineTo(11 * u, 2 * u); lineTo(1.5f * u, 2 * u); close() }, color)
                drawPath(Path().apply { moveTo(-1.5f * u, -9 * u); lineTo(-9 * u, 2 * u); lineTo(-1.5f * u, 2 * u); close() }, color)
            }
        }
    }
}

/** The imprint's mark: a pen nib. */
private fun DrawScope.nib(c: Offset, u: Float, color: Color) {
    translate(c.x, c.y) {
        val p = Path().apply { moveTo(0f, -10 * u); lineTo(6 * u, 0f); lineTo(0f, 10 * u); lineTo(-6 * u, 0f); close() }
        drawPath(p, color)
        drawLine(Color(0xFFF7F2E7), Offset(0f, -2 * u), Offset(0f, 9 * u), .9f * u)
        drawCircle(Color(0xFFF7F2E7), 1.4f * u, Offset(0f, -2 * u))
    }
}
