package com.deysdeveloper.mutualfundapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary                = Primary40,
    onPrimary              = OnPrimary40,
    primaryContainer       = PrimaryContainer40,
    onPrimaryContainer     = OnPrimaryContainer40,
    secondary              = Secondary40,
    onSecondary            = OnSecondary40,
    secondaryContainer     = SecondaryContainer40,
    onSecondaryContainer   = OnSecondaryContainer40,
    tertiary               = Tertiary40,
    onTertiary             = OnTertiary40,
    tertiaryContainer      = TertiaryContainer40,
    onTertiaryContainer    = OnTertiaryContainer40,
    background             = Color(0xFFF5F8FF),
    onBackground           = Color(0xFF101828),
    surface                = Color(0xFFFFFFFF),
    onSurface              = Color(0xFF101828),
    surfaceVariant         = Color(0xFFE4EAFE),
    onSurfaceVariant       = Color(0xFF44546A),
    outline                = Color(0xFF8FA3BC),
    outlineVariant         = Color(0xFFCAD5E2),
)

private val DarkColorScheme = darkColorScheme(
    primary                = Primary80,
    onPrimary              = OnPrimary80,
    primaryContainer       = PrimaryContainer80,
    onPrimaryContainer     = OnPrimaryContainer80,
    secondary              = Secondary80,
    onSecondary            = OnSecondary80,
    secondaryContainer     = SecondaryContainer80,
    onSecondaryContainer   = OnSecondaryContainer80,
    tertiary               = Tertiary80,
    onTertiary             = OnTertiary80,
    tertiaryContainer      = TertiaryContainer80,
    onTertiaryContainer    = OnTertiaryContainer80,
    background             = Color(0xFF0E1520),
    onBackground           = Color(0xFFE8EDF7),
    surface                = Color(0xFF151D2E),
    onSurface              = Color(0xFFE8EDF7),
    surfaceVariant         = Color(0xFF1C2A40),
    onSurfaceVariant       = Color(0xFF8FA3BC),
    outline                = Color(0xFF3D5270),
    outlineVariant         = Color(0xFF1C2A40),
)

@Composable
fun MutualFundAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
