package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// CENTRALIZED DESIGN SYSTEM COLOR DEFINITIONS
// ==========================================

data class CssThemeVariables(
    val bg: Color = Color(0xFF0F1115),           // Apple deep dark background
    val surface: Color = Color(0xFF1C2230),      // Apple card container surface
    val primary: Color = Color(0xFF4F8CFF),      // Apple primary blue tint
    val secondary: Color = Color(0xFFA7AFBD),    // Apple secondary text / elements
    val accent: Color = Color(0xFFFFB020),       // Apple warning gold
    val danger: Color = Color(0xFFFF5A5F),       // Apple error crimson red
    val textGray: Color = Color(0xFFA7AFBD),     // Apple soft text gray secondary
    val textWhite: Color = Color(0xFFFFFFFF),    // Apple pure white primary text
    val border: Color = Color(0xFF171A21),       // Apple surface separator
    val credit: Color = Color(0xFF8B5CF6),       // Soft purple
    val sip: Color = Color(0xFF4F8CFF),          // Soft blue
    val isLight: Boolean = false
) {
    fun get(name: String): Color {
        return when (name) {
            "--bg" -> bg
            "--surface" -> surface
            "--primary" -> primary
            "--secondary" -> secondary
            "--accent" -> accent
            "--danger" -> danger
            "--text-gray" -> textGray
            "--text-white" -> textWhite
            "--border" -> border
            "--credit" -> credit
            "--sip" -> sip
            else -> Color.Unspecified
        }
    }
}

val LocalCssThemeVariables = staticCompositionLocalOf { CssThemeVariables() }

@Composable
fun cssVar(name: String): Color {
    return LocalCssThemeVariables.current.get(name)
}

// Proxied Semantic Color Tokens accessed dynamically by pages
val NavyBg: Color @Composable get() = cssVar("--bg")
val SurfaceBlue: Color @Composable get() = cssVar("--surface")
val NeonGreen: Color @Composable get() = cssVar("--primary")
val DeepPurple: Color @Composable get() = cssVar("--secondary")
val AccentOrange: Color @Composable get() = cssVar("--accent")
val DangerRed: Color @Composable get() = cssVar("--danger")
val TextGray: Color @Composable get() = cssVar("--text-gray")
val TextWhite: Color @Composable get() = cssVar("--text-white")
val BorderColor: Color @Composable get() = cssVar("--border")
val CreditPurple: Color @Composable get() = cssVar("--credit")
val SipBlue: Color @Composable get() = cssVar("--sip")

// Reusable Banking-specific semantic color tokens
@Immutable
data class MyFinColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val positiveAmount: Color,
    val negativeAmount: Color,
    val creditAccent: Color,
    val sipAccent: Color,
)

val LocalMyFinColors = staticCompositionLocalOf {
    MyFinColors(
        success = Color.Unspecified,
        warning = Color.Unspecified,
        info = Color.Unspecified,
        cardBackground = Color.Unspecified,
        cardBorder = Color.Unspecified,
        positiveAmount = Color.Unspecified,
        negativeAmount = Color.Unspecified,
        creditAccent = Color.Unspecified,
        sipAccent = Color.Unspecified
    )
}

val MaterialTheme.myFinColors: MyFinColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMyFinColors.current

// Main Theme used throughout the entire application supporting multiple dynamic theme slots
@Composable
fun WealthPulseTheme(
    variables: CssThemeVariables = CssThemeVariables(),
    content: @Composable () -> Unit
) {
    val myFinColors = if (variables.isLight) {
        MyFinColors(
            success = MoneyPositive,
            warning = AccentSunsetOrange,
            info = InfoSkyBlue,
            cardBackground = variables.surface,
            cardBorder = variables.border,
            positiveAmount = MoneyPositive,
            negativeAmount = MoneyNegative,
            creditAccent = variables.credit,
            sipAccent = variables.sip
        )
    } else {
        MyFinColors(
            success = MoneyPositive,
            warning = AccentAmber,
            info = InfoSkyBlue,
            cardBackground = variables.surface,
            cardBorder = variables.border,
            positiveAmount = MoneyPositive,
            negativeAmount = MoneyNegative,
            creditAccent = variables.credit,
            sipAccent = variables.sip
        )
    }

    val scheme = if (variables.isLight) {
        lightColorScheme(
            background = variables.bg,
            surface = variables.surface,
            primary = variables.primary,
            secondary = variables.secondary,
            tertiary = variables.accent,
            onBackground = variables.textWhite,
            onSurface = variables.textWhite,
            outline = variables.border,
            outlineVariant = variables.border.copy(alpha = 0.5f),
            error = variables.danger,
            onError = variables.textWhite
        )
    } else {
        darkColorScheme(
            background = variables.bg,
            surface = variables.surface,
            primary = variables.primary,
            secondary = variables.secondary,
            tertiary = variables.accent,
            onBackground = variables.textWhite,
            onSurface = variables.textWhite,
            outline = variables.border,
            outlineVariant = variables.border.copy(alpha = 0.5f),
            error = variables.danger,
            onError = variables.textWhite
        )
    }

    CompositionLocalProvider(
        LocalCssThemeVariables provides variables,
        LocalMyFinColors provides myFinColors
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

// Backup theme alias pointing to prime theme selector for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val defaultVariables = if (darkTheme) CssThemeVariables() else CssThemeVariables(
        bg = Color(0xFFF7F8FA),
        surface = Color(0xFFFFFFFF),
        primary = Color(0xFF4F8CFF),
        secondary = Color(0xFF667085),
        accent = Color(0xFFFFB020),
        danger = Color(0xFFFF5A5F),
        textGray = Color(0xFF667085),
        textWhite = Color(0xFF101828),
        border = Color(0xFFE6E8EE),
        credit = Color(0xFF8B5CF6),
        sip = Color(0xFF4F8CFF),
        isLight = true
    )
    WealthPulseTheme(variables = defaultVariables, content = content)
}

