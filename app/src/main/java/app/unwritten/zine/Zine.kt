package app.unwritten.zine

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import app.unwritten.domain.Book
import app.unwritten.ui.Words
import app.unwritten.ui.art.Face
import app.unwritten.ui.art.drawCover
import app.unwritten.ui.theme.Serif
import java.io.File

/**
 * A book printed as a one-sheet zine: eight little pages on A4, folded and cut once, no glue. The top row is
 * upside down, as the fold needs: 5 4 3 2 over 6 7 8 1.
 */
object Zine {
    private const val W = 842f
    private const val H = 595f

    fun pdf(ctx: Context, b: Book, w: Words, imprint: String): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), 1).create())
        val density = Density(1f)
        val tm = TextMeasurer(createFontFamilyResolver(ctx), density, LayoutDirection.Ltr)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(page.canvas), Size(W, H)) {
            drawRect(Color.White)
            val pw = W / 4; val ph = H / 2
            val top = listOf(5, 4, 3, 2)
            val bottom = listOf(6, 7, 8, 1)
            for ((i, n) in top.withIndex()) panel(i * pw, 0f, pw, ph, flip = true) { panelContent(n, b, w, imprint, tm, pw, ph) }
            for ((i, n) in bottom.withIndex()) panel(i * pw, ph, pw, ph, flip = false) { panelContent(n, b, w, imprint, tm, pw, ph) }
            // Fold and cut guides.
            val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            for (i in 1..3) drawLine(Color(0xFFCCCCCC), Offset(i * pw, 0f), Offset(i * pw, H), .5f, pathEffect = dash)
            drawLine(Color(0xFFCCCCCC), Offset(0f, ph), Offset(W, ph), .5f, pathEffect = dash)
            drawLine(Color(0xFF999999), Offset(pw, ph), Offset(3 * pw, ph), .8f)
        }
        doc.finishPage(page)
        val dir = File(ctx.cacheDir, "zines").apply { mkdirs() }
        val name = b.title.replace(Regex("[^\\p{L}\\p{N} ]"), "").trim().replace(' ', '_').take(40).ifEmpty { "zine" }
        val f = File(dir, "$name.pdf")
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }

    fun share(ctx: Context, b: Book, w: Words, imprint: String) {
        val f = pdf(ctx, b, w, imprint)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
        val send = Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri).putExtra(Intent.EXTRA_SUBJECT, b.title)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ctx.startActivity(Intent.createChooser(send, w.t("Зин: «${b.title}»", "Zine: “${b.title}”")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun DrawScope.panel(x: Float, y: Float, pw: Float, ph: Float, flip: Boolean, content: DrawScope.() -> Unit) {
        translate(x, y) {
            if (flip) rotate(180f, Offset(pw / 2, ph / 2)) { content() } else content()
        }
    }

    private fun DrawScope.panelContent(n: Int, b: Book, w: Words, imprint: String, tm: TextMeasurer, pw: Float, ph: Float) {
        val m = 18f
        val body = TextStyle(fontFamily = Serif, fontSize = 9.sp, lineHeight = 13.sp, color = Color(0xFF1A1A1A))
        val italic = body.copy(fontStyle = FontStyle.Italic)
        val center = body.copy(textAlign = TextAlign.Center)
        fun block(s: String, style: TextStyle, middle: Boolean = true) {
            val l = tm.measure(s, style, overflow = TextOverflow.Ellipsis, maxLines = 22, constraints = Constraints(maxWidth = (pw - 2 * m).toInt()))
            val ty = if (middle) (ph - l.size.height) / 2f else m + 6f
            drawText(l, topLeft = Offset(m, ty))
        }
        when (n) {
            1 -> {
                val cw = pw - 2 * m
                val ch = cw * 1.5f
                translate(m, (ph - ch) / 2f) { drawIntoCover(Face(b), w, imprint, tm, Size(cw, ch)) }
            }
            2 -> block(b.epigraph.ifBlank { w.t("Тому, кто это найдёт", "For whoever finds this") }, italic.copy(textAlign = TextAlign.End))
            3 -> block(b.first.ifBlank { "…" }, body.copy(fontSize = 10.5.sp, lineHeight = 15.sp))
            4 -> if (b.chapters.isNotEmpty()) block(w.t("Содержание", "Contents").uppercase() + "\n\n" + b.chapters.joinToString("\n"), center)
            else block(w.t("[страницы утрачены]", "[pages missing]"), italic.copy(textAlign = TextAlign.Center))
            5 -> block((b.middle.ifBlank { "…" }) + if (b.margin.isNotBlank()) "\n\n— ${b.margin}" else "", body, middle = false)
            6 -> block((b.last.ifBlank { "" }) + "\n\n❦", italic.copy(textAlign = TextAlign.Center))
            7 -> block(
                "${b.author.ifBlank { w.t("Автор неизвестен", "Author unknown") }}\n${b.title}\n${w.kind(b.kind)}, ${w.pages(b.pages)}\n\n" +
                    w.t("Издательство «${w.imprint(imprint)}»\nТираж: 1 экз.\nКниги не существует.", "${w.imprint(imprint)}\nPrint run: 1 copy.\nThis book does not exist."),
                center.copy(fontSize = 7.5.sp, lineHeight = 11.sp),
            )
            8 -> {
                drawRect(Color.hsl(Face(b).hue, .25f, .9f), Offset(6f, 6f), Size(pw - 12f, ph - 12f))
                block(b.blurb, body)
                drawRect(Color(0xFF999999), Offset(6f, 6f), Size(pw - 12f, ph - 12f), style = Stroke(.4f))
            }
        }
        if (n in 2..7) {
            val l = tm.measure("$n", body.copy(fontSize = 7.sp, color = Color(0xFF777777)))
            drawText(l, topLeft = Offset((pw - l.size.width) / 2f, ph - 16f))
        }
    }

    /** The cover drawn into a box of [s] at the current origin. */
    private fun DrawScope.drawIntoCover(f: Face, w: Words, imprint: String, tm: TextMeasurer, s: Size) {
        val outer = drawContext.size
        drawContext.size = s
        drawCover(f, w, imprint, tm)
        drawContext.size = outer
    }
}
