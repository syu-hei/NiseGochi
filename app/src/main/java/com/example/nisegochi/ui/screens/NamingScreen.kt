package com.example.nisegochi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.ui.viewmodel.PetViewModel

@Composable
fun NamingScreen(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val currentName by viewModel.namingName.collectAsState()
    val selectedLetterIndex by viewModel.namingIndex.collectAsState()
    val letters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NAME YOUR PET",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = currentName + letters[selectedLetterIndex] + "_",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "A: NEXT  B: SELECT",
            color = Color.Black,
            fontSize = 12.sp
        )
        Text(
            text = "C: DONE",
            color = Color.Black,
            fontSize = 12.sp
        )
    }
}
