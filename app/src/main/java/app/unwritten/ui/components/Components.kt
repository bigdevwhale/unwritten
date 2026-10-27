package app.unwritten.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unwritten.BuildConfig
import app.unwritten.ui.theme.C
import app.unwritten.ui.theme.T
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Seconds since the screen appeared, every frame: rain, candle, steam, dust. Still in screenshot builds. */
@Composable
fun rememberSeconds(): State<Float> {
    val t = remember { mutableFloatStateOf(if (BuildConfig.SHOTS) 3.3f else 0f) }
    if (!BuildConfig.SHOTS) LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { t.floatValue = (it - start) / 1e9f }
    }
    return t
}

/** Cream paper with a few specks and a soft edge. */
fun Modifier.paper(color: Color = C.paper) = this.background(color).drawBehind {
    val r = Random(7)
    val n = (size.width * size.height / 9000f).toInt().coerceAtMost(500)
    repeat(n) { drawCircle(C.ink2.copy(alpha = .03f + r.nextFloat() * .04f), .6f + r.nextFloat() * 1.2f, Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)) }
    drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .05f), Color.Transparent), 0f, 18.dp.toPx()))
}

enum class Kind { BRASS, GHOST, INK, LIGHT }

/** A rounded brass button, or a ghost of one. */
@Composable
fun Btn(text: String, modifier: Modifier = Modifier, kind: Kind = Kind.BRASS, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val bg: Modifier = when {
        !enabled -> Modifier.background(C.muted.copy(alpha = .35f))
        kind == Kind.BRASS -> Modifier.shadow(4.dp, shape).background(Brush.verticalGradient(listOf(Color(0xFFF6D394), Color(0xFFE7B664))))
        kind == Kind.INK -> Modifier.background(C.ink)
        kind == Kind.LIGHT -> Modifier.background(C.lamp.copy(alpha = .10f)).border(1.dp, C.lamp.copy(alpha = .45f), shape)
        else -> Modifier.border(1.5.dp, C.ink.copy(alpha = .7f), shape)
    }
    val fg = when { !enabled -> C.muted; kind == Kind.BRASS -> Color(0xFF3A2414); kind == Kind.INK -> C.paper; kind == Kind.LIGHT -> C.lamp; else -> C.ink }
    Box(modifier.clip(shape).then(bg).clickable(enabled = enabled, onClick = onClick).height(48.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        // One line and one height for every button: a long label shrinks a little instead of wrapping.
        BasicText(
            text, style = T.Button.copy(color = fg, textAlign = TextAlign.Center), maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = T.Button.fontSize, stepSize = .5.sp),
        )
    }
}

@Composable
fun Chip(text: String, on: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val (bg, fg, edge) = if (on) Triple(C.ink, C.paper, C.ink) else Triple(Color.Transparent, C.ink, C.ink.copy(alpha = .45f))
    Box(modifier.clip(shape).background(bg).border(1.dp, edge, shape).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, style = T.Body.copy(color = fg, fontSize = 14.sp, lineHeight = 18.sp))
    }
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier, color: Color = C.muted) =
    Text(text, style = T.Label.copy(color = color), modifier = modifier.padding(top = 16.dp, bottom = 4.dp))

@Composable
fun Gap(h: Int = 10) = Spacer(Modifier.height(h.dp))

@Composable
fun Flow(modifier: Modifier = Modifier, gap: Dp = 6.dp, content: @Composable () -> Unit) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) { content() }
}

/** A line to write on: ruled paper under the words. */
@Composable
fun Field(
    value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, style: TextStyle = T.Body,
    minLines: Int = 1, max: Int = 200, single: Boolean = false, onDone: (() -> Unit)? = null,
) {
    val lineH = with(androidx.compose.ui.platform.LocalDensity.current) { (style.lineHeight.takeIf { it.isSp } ?: 23.sp).toPx() }
    Box(
        modifier.fillMaxWidth().drawBehind {
            var y = lineH + 4.dp.toPx()
            while (y <= size.height + 1) { drawLine(C.line.copy(alpha = .8f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += lineH }
        }.padding(top = 2.dp, bottom = 6.dp),
    ) {
        if (value.isEmpty()) Text(hint, style = style.copy(color = C.muted.copy(alpha = .75f)))
        BasicTextField(
            value, { onChange(if (single) it.replace("\n", " ").take(max) else it.take(max)) }, Modifier.fillMaxWidth(), textStyle = style, minLines = minLines,
            singleLine = single, cursorBrush = SolidColor(C.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        )
    }
}

/** A sheet of paper over the shop, taped at the top, with a title and a way back. */
@Composable
fun Sheet(title: String, onClose: () -> Unit, closeLabel: String, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(C.scrim).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(start = 10.dp, end = 10.dp, top = 18.dp, bottom = 10.dp).shadow(10.dp, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).paper()) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = T.Heading.copy(fontSize = 22.sp), modifier = Modifier.weight(1f))
                Text(closeLabel, style = T.Label.copy(color = C.ink2), modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClose).padding(10.dp))
            }
            val inner = Modifier.fillMaxWidth().weight(1f).then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(horizontal = 20.dp, vertical = 6.dp)
            Column(inner, content = content)
        }
        // A strip of tape holding the sheet up.
        Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp).rotate(-2f).background(Color(0xA6F0E1B4)).padding(horizontal = 46.dp, vertical = 10.dp))
    }
}

/** Short messages: a slip of paper in a hand. */
class Toaster {
    var text by mutableStateOf<String?>(null)
    var seq by mutableIntStateOf(0)
    fun show(s: String) { text = s; seq++ }
}

val LocalToast = compositionLocalOf { Toaster() }

@Composable
fun BoxScope.ToastHost(t: Toaster) {
    LaunchedEffect(t.seq) { if (t.text != null) { delay(3200); t.text = null } }
    val s = t.text ?: return
    Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
        Text(
            s, style = T.Note.copy(fontSize = 22.sp, lineHeight = 24.sp, textAlign = TextAlign.Center),
            modifier = Modifier.rotate(-1f).shadow(6.dp).background(C.paperLight).clickable { t.text = null }.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}
