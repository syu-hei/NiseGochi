package com.example.nisegochi.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.example.nisegochi.R

@Composable
fun IconBar(
    icons: List<Int>,
    selectedIndex: Int, // 1-based index
    modifier: Modifier = Modifier,
    attentionActive: Boolean = false,
    pantryActive: Boolean = false,
    startIndex: Int = 1 // The first icon's index in the global icon list
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            }
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        icons.forEachIndexed { index, iconId ->
            val globalIndex = startIndex + index
            val isSelected = globalIndex == selectedIndex
            val isAttention = iconId == R.drawable.attention_icon
            val isFlickering = (isAttention && attentionActive) || (globalIndex == 1 && pantryActive)
            
            // Retro UI: Selected icons have a solid dark background
            val lcdOnColor = Color(0xFF2B3324) // Darker "ink" color
            val lcdOffColor = Color(0xFF2B3324).copy(alpha = 0.1f) // Ghosting effect
            val lcdBgColor = Color(0xFF9BAB8D) // LCD background

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) lcdOnColor else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(6.dp)
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = FilterQuality.None,
                    colorFilter = when {
                        isSelected -> ColorFilter.tint(lcdBgColor) // Inverted
                        isFlickering -> ColorFilter.tint(if (flickerAlpha > 0.5f) lcdOnColor else lcdOffColor)
                        else -> ColorFilter.tint(lcdOffColor)
                    }
                )
            }
        }
    }
}
