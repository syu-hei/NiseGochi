package com.example.nisegochi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color

val LocalPixelScale = compositionLocalOf { 1f }

@Composable
fun PixelCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(2f) // 32:16
            .clipToBounds()
            .background(Color(0xFF9BAB8D)) // Classic retro LCD color
    ) {
        // Calculate scale: width in pixels / 32 logical pixels
        val scale = constraints.maxWidth / 32f
        
        CompositionLocalProvider(LocalPixelScale provides scale) {
            content()
        }
    }
}
