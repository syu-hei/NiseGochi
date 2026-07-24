package com.example.nisegochi.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints

@Composable
fun PixelSprite(
    drawableId: Int,
    x: Int,
    y: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scale = LocalPixelScale.current
    
    val bitmap = remember(drawableId) {
        val options = BitmapFactory.Options().apply { inScaled = false }
        BitmapFactory.decodeResource(context.resources, drawableId, options)
    }
    
    if (bitmap == null) return

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    
    Image(
        bitmap = imageBitmap,
        contentDescription = null,
        modifier = modifier
            .layout { measurable, constraints ->
                // Account for 30x asset scaling
                val logicalWidth = bitmap.width / 30f
                val logicalHeight = bitmap.height / 30f
                
                val width = (logicalWidth * scale).toInt()
                val height = (logicalHeight * scale).toInt()
                
                val placeable = measurable.measure(Constraints.fixed(width, height))
                
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place((x * scale).toInt(), (y * scale).toInt())
                }
            },
        filterQuality = FilterQuality.None,
        contentScale = ContentScale.FillBounds
    )
}
