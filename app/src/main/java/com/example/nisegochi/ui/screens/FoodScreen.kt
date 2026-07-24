package com.example.nisegochi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.ui.viewmodel.PetViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Icon

@Composable
fun FoodScreen(viewModel: PetViewModel) {
    val selection by viewModel.foodSelection.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FEED",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodOption(
                label = "MEAL",
                isSelected = selection == 0,
                icon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = if (selection == 0) Color.Red else Color.Gray) }
            )
            
            FoodOption(
                label = "SNACK",
                isSelected = selection == 1,
                icon = { Icon(Icons.Default.Cake, contentDescription = null, tint = if (selection == 1) Color.Red else Color.Gray) }
            )
        }
    }
}

@Composable
fun FoodOption(label: String, isSelected: Boolean, icon: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Red else Color.Black
        )
        if (isSelected) {
            Text(text = "▲", color = Color.Red)
        }
    }
}
