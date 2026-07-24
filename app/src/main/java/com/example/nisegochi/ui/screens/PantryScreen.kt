package com.example.nisegochi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.ui.viewmodel.PetViewModel
import com.example.nisegochi.ui.components.PixelCanvas
import com.example.nisegochi.ui.components.PixelSprite
import androidx.compose.ui.platform.LocalContext

@Composable
fun PantryScreen(viewModel: PetViewModel) {
    val state by viewModel.petState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PANTRY",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (state.pantry.isEmpty()) {
            Text(text = "EMPTY", fontSize = 18.sp, color = Color.Gray)
        } else {
            val selectedItem = state.pantry.getOrNull(state.selectedPantryIndex) ?: state.pantry.first()
            
            PixelCanvas(modifier = Modifier.size(64.dp)) {
                val resId = context.resources.getIdentifier(selectedItem, "drawable", context.packageName)
                if (resId != 0) {
                    PixelSprite(drawableId = resId, x = 0, y = 0)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = selectedItem.replace("_", " ").uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${state.selectedPantryIndex + 1} / ${state.pantry.size}",
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}
