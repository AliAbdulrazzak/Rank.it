package com.rankit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark color palette
val Purple80 = Color(0xFFDC2626)
val PurpleGrey80 = Color(0xFF4B5563)
val Pink80 = Color(0xFF16A34A)

val BackgroundDark = Color(0xFF0A0A0F)
val SurfaceDark = Color(0xFF13131F)
val SurfaceVariantDark = Color(0xFF1E1E2E)
val AccentPurple = Color(0xFFDC2626)
val AccentBlue = Color(0xFF16A34A)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF4B5563)
val UpvoteGreen = Color(0xFF22C55E)
val DownvoteRed = Color(0xFFEF4444)
val CardBorder = Color(0xFF1F2937)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentBlue,
    tertiary = Pink80,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun RankItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
