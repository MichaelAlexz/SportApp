package com.sportapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryDark,
    secondary = OrangeAccent,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = ErrorRed,
    onError = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun SportAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = SportAppTypography,
        content = content
    )
}
