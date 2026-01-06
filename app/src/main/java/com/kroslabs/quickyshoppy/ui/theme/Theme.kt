package com.kroslabs.quickyshoppy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green200,
    onPrimaryContainer = Green700,
    secondary = Green700,
    onSecondary = Color.White,
    background = Gray100,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    error = Red500,
    onError = Color.White
)

@Composable
fun QuickyShoppyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
