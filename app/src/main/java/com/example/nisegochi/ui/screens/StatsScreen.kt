package com.example.nisegochi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.R
import com.example.nisegochi.ui.components.PixelCanvas
import com.example.nisegochi.ui.components.PixelSprite
import com.example.nisegochi.ui.viewmodel.PetViewModel

@Composable
fun StatsScreen(viewModel: PetViewModel, modifier: Modifier = Modifier) {
    val petState by viewModel.petState.collectAsState()
    
    var animatedStomach by remember { mutableIntStateOf(0) }
    var animatedHappy by remember { mutableIntStateOf(0) }

    LaunchedEffect(petState.stomach, petState.happy) {
        animatedStomach = 0
        animatedHappy = 0
        for (i in 1..petState.stomach) {
            kotlinx.coroutines.delay(100)
            animatedStomach = i
        }
        for (i in 1..petState.happy) {
            kotlinx.coroutines.delay(100)
            animatedHappy = i
        }
    }
    
    PixelCanvas(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 2.dp, top = 2.dp)) {
            Text(text = "AGE: ${petState.age}yr", color = Color.Black, fontSize = 12.sp, lineHeight = 12.sp)
            Text(text = "WEIGHT: ${petState.weight}oz", color = Color.Black, fontSize = 12.sp, lineHeight = 12.sp)
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Hunger Section
            Box(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                Text(text = "HUNGER", color = Color.Black, fontSize = 12.sp)
            }
            
            // Happy Section
            Box(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                Text(text = "HAPPY", color = Color.Black, fontSize = 12.sp)
            }
        }

        // Draw hunger hearts
        for (i in 0 until 4) {
            val resId = if (animatedStomach > i) R.drawable.full_heart else R.drawable.empty_heart
            // x position starts after "HUNGER" text roughly (adjust as needed)
            PixelSprite(resId, 18 + i * 3, 7)
        }

        // Draw happy hearts
        for (i in 0 until 4) {
            val resId = if (animatedHappy > i) R.drawable.full_heart else R.drawable.empty_heart
            // x position starts after "HAPPY" text roughly
            PixelSprite(resId, 18 + i * 3, 11)
        }
    }
}
