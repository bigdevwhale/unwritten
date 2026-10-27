package app.unwritten.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.unwritten.R

/** A bookshop in the evening: dark wood and wallpaper, lamplight, cream paper, pencil and violet ink. */
@Immutable
object C {
    val wall = Color(0xFF33261B)
    val wallDot = Color(0x1AD6AA6E)
    val wood = Color(0xFF6A4528)
    val woodDark = Color(0xFF4A3220)
    val woodLight = Color(0xFF8A603A)
    val shelfBack = Color(0xFF2B1C12)
    val counter = Color(0xFF7A5232)
    val lamp = Color(0xFFF3C77A)
    val lampHi = Color(0xFFFFE2A8)
    val lampText = Color(0xFFE6C995)
    val paper = Color(0xFFF7EFDC)
    val paperLight = Color(0xFFFBF5E6)
    val paperDark = Color(0xFFEADDC0)
    val line = Color(0xFFC9B796)
    val ink = Color(0xFF2B2118)
    val ink2 = Color(0xFF5A4A3A)
    val muted = Color(0xFF8A7A66)
    val accent = Color(0xFFB5502F)
    val stamp = Color(0xFF6B3FA0)
    val pencil = Color(0xFF5D6B7A)
    val pin = Color(0xFFB8342A)
    val scrim = Color(0xCC1A120C)
}

val Serif = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_medium, FontWeight.Bold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)
/** Customers' notes, pencil in the margins, the bargain-box tag. */
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))
/** The covers: a classic series, a three-band paperback, pulp, a typewriter. */
val Classic = FontFamily(Font(R.font.forum_regular, FontWeight.Normal))
val Grotesk = FontFamily(Font(R.font.inter_semibold, FontWeight.Bold))
val Narrow = FontFamily(Font(R.font.pt_sans_narrow_bold, FontWeight.Bold))
val Typewriter = FontFamily(Font(R.font.pt_mono, FontWeight.Normal))

object T {
    val Title = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 32.sp, color = C.ink)
    val Heading = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp, color = C.ink)
    val Body = TextStyle(fontFamily = Serif, fontSize = 16.sp, lineHeight = 23.sp, color = C.ink)
    val BodyB = Body.copy(fontWeight = FontWeight.Bold)
    val Italic = Body.copy(fontStyle = FontStyle.Italic)
    val Small = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 13.5.sp, lineHeight = 19.sp, color = C.ink2)
    val Label = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 14.sp, lineHeight = 18.sp, color = C.muted)
    val Button = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp)
    val Note = TextStyle(fontFamily = Hand, fontSize = 25.sp, lineHeight = 27.sp, color = C.ink)
    val Pencil = TextStyle(fontFamily = Hand, fontSize = 21.sp, lineHeight = 23.sp, color = C.pencil)
    val Card = TextStyle(fontFamily = Typewriter, fontSize = 13.sp, lineHeight = 18.sp, color = C.ink)
}

@Composable
fun ShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.lamp, onPrimary = C.ink, background = C.wall, surface = C.paper, onSurface = C.ink,
            onBackground = C.paper, outline = C.line, error = C.accent,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
