package com.engage.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF16222F)
val NavySoft = Color(0xFF1E2F42)
val Gold = Color(0xFFB8892B)
val GoldLight = Color(0xFFF6EFE0)
val WarmInk = Color(0xFF7A5C14)
val SubText = Color(0xFF5D6B7A)
val HairLine = Color(0xFFD8E0E9)
val PageBg = Color(0xFFF4F6F9)
val HitColor = Color(0xFFA8382C)
val UpColor = Color(0xFF2F6B4F)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = WarmInk,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = WarmInk,
    tertiary = UpColor,
    background = PageBg,
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy,
    surfaceVariant = Color(0xFFEEF3F8),
    onSurfaceVariant = SubText,
    outline = HairLine,
    outlineVariant = Color(0xFFEEF2F6),
    error = HitColor,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD9B76A),
    onPrimary = Color(0xFF20180A),
    primaryContainer = Color(0xFF2C3B4D),
    onPrimaryContainer = Color(0xFFEADFC4),
    secondary = Color(0xFFC9A45C),
    onSecondary = Color(0xFF1C1607),
    secondaryContainer = Color(0xFF3A2F18),
    onSecondaryContainer = Color(0xFFF0E3C6),
    tertiary = Color(0xFF8FC9AC),
    background = Color(0xFF10171F),
    onBackground = Color(0xFFE6ECF2),
    surface = Color(0xFF17212B),
    onSurface = Color(0xFFE6ECF2),
    surfaceVariant = Color(0xFF22303D),
    onSurfaceVariant = Color(0xFFA9B7C4),
    outline = Color(0xFF3B4A59),
    outlineVariant = Color(0xFF2A3743),
    error = Color(0xFFE79A90),
    onError = Color(0xFF3A100C),
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun EngageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}