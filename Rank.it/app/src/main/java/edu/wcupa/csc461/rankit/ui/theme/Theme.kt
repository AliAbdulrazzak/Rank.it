package edu.wcupa.csc461.rankit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class AppTheme(val displayName: String) {
    DEFAULT("Default"),
    BLUE("Blue & Orange"),
    PURPLE("Purple & Yellow"),
    OCEAN("Ocean Breeze"),
    FOREST("Forest Green")
}

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Red80,
    secondary = RedGrey80,
    tertiary = Green80
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Red40,
    secondary = RedGrey40,
    tertiary = Green40
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Orange80
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Orange40
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Yellow80
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Yellow40
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = TealGrey80,
    tertiary = Cyan80
)

private val OceanLightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = TealGrey40,
    tertiary = Cyan40
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestGreen80,
    secondary = ForestGrey80,
    tertiary = Emerald80
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestGreen40,
    secondary = ForestGrey40,
    tertiary = Emerald40
)

@Composable
fun RankitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    appTheme: AppTheme = AppTheme.DEFAULT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> when (appTheme) {
            AppTheme.DEFAULT -> DefaultDarkColorScheme
            AppTheme.BLUE -> BlueDarkColorScheme
            AppTheme.PURPLE -> PurpleDarkColorScheme
            AppTheme.OCEAN -> OceanDarkColorScheme
            AppTheme.FOREST -> ForestDarkColorScheme
        }
        else -> when (appTheme) {
            AppTheme.DEFAULT -> DefaultLightColorScheme
            AppTheme.BLUE -> BlueLightColorScheme
            AppTheme.PURPLE -> PurpleLightColorScheme
            AppTheme.OCEAN -> OceanLightColorScheme
            AppTheme.FOREST -> ForestLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontScale),
        content = content
    )
}
