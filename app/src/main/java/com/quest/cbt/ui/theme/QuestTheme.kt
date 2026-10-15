package com.quest.cbt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Quest CBT Brand Colors
val DarkNavyBg = Color(0xFF0F1520)
val CardSurface = Color(0xFF1B2230)
val CardSurfaceElevated = Color(0xFF232D40)
val CardBorder = Color(0xFF2A354A)
val CardBorderSubtle = Color(0xFF1E2838)
val PeriwinkleAccent = Color(0xFFAEC6FF)
val OnPeriwinkle = Color(0xFF0F1520)

// Text Colors
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// NTA Status Colors
val NtaNotVisited = Color(0xFF6B7280)
val NtaNotAnswered = Color(0xFFEF4444)
val NtaAnswered = Color(0xFF22C55E)
val NtaMarked = Color(0xFFA855F7)
val NtaAnsweredAndMarked = Color(0xFFA855F7) // with green tick badge

// Subject Accent Colors
val PhysicsColor = Color(0xFF38BDF8)
val ChemistryColor = Color(0xFFF472B6)
val MathsColor = Color(0xFFFBBF24)

val QuestColorScheme: ColorScheme = darkColorScheme(
    primary = PeriwinkleAccent,
    onPrimary = OnPeriwinkle,
    primaryContainer = Color(0xFF243656),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF7DD3FC),
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFFC084FC),
    onTertiary = Color(0xFF3B0764),
    background = DarkNavyBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardBorderSubtle,
    error = NtaNotAnswered,
    onError = Color.White
)

val QuestShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun QuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = QuestColorScheme,
        shapes = QuestShapes,
        content = content
    )
}
