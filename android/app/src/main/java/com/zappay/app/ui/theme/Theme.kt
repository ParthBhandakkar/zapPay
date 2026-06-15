package com.zappay.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = White,
    primaryContainer = Purple50,
    secondary = Green500,
    onSecondary = White,
    background = Gray50,
    surface = White,
    onBackground = Gray900,
    onSurface = Gray900,
    outline = Gray200,
    error = Red500,
    onError = White,
)

@Composable
fun ZapPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
