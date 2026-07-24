package com.example.nisegochi.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.ui.components.PixelCanvas
import com.example.nisegochi.ui.viewmodel.PetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockScreen(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    val petState by viewModel.petState.collectAsState()
    val isSetMode by viewModel.isClockSetMode.collectAsState()
    val selection by viewModel.clockSelection.collectAsState()
    
    val birthTime = petState.birthTimeMillis
    val totalSeconds = petState.totalTimeSeconds
    val currentTimeMillis = birthTime + (totalSeconds * 1000).toLong()
    val date = Date(currentTimeMillis)
    
    val hours = SimpleDateFormat("hh", Locale.getDefault()).format(date)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(date)
    val amPm = SimpleDateFormat("a", Locale.getDefault()).format(date)
    
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    PixelCanvas(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSetMode) {
                    Text(
                        text = "SET",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = hours,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.alpha(if (isSetMode && selection == 0) blinkAlpha else 1f)
                    )
                    Text(
                        text = ":",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = minutes,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.alpha(if (isSetMode && selection == 1) blinkAlpha else 1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = amPm.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
