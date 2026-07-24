package com.example.nisegochi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nisegochi.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import com.example.nisegochi.domain.GameState
import com.example.nisegochi.ui.components.PixelCanvas
import com.example.nisegochi.ui.components.PixelSprite
import com.example.nisegochi.ui.viewmodel.PetViewModel

@Composable
fun GameScreen(viewModel: PetViewModel) {
    val state by viewModel.petState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.gameType == "GiftFinding") {
            SantaGameView(state)
        } else {
            HigherLowerGameView(state)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (state.gameState == GameState.Playing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (state.gameType == "GiftFinding") {
                    GameButton("NEXT") { viewModel.moveChimneyArrow() }
                    GameButton("CHOOSE") { viewModel.selectChimney() }
                } else {
                    GameButton("HIGHER") { viewModel.guessHigher() }
                    GameButton("LOWER") { viewModel.guessLower() }
                }
            }
        }
    }
}

@Composable
fun HigherLowerGameView(state: com.example.nisegochi.domain.PetState) {
    PixelCanvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        when (state.gameState) {
            GameState.Intro -> {
                val frame = if (state.totalTimeSeconds.toInt() % 2 == 0) 1 else 2
                val resId = getResId(state.character, "play", frame)
                if (resId != 0) PixelSprite(resId, 8, 0)
                PixelSprite(com.example.nisegochi.R.drawable.full_heart, 0, 0)
                PixelSprite(com.example.nisegochi.R.drawable.empty_heart, 24, 0)
            }
            GameState.Playing -> {
                val resId = getResId("num${state.givenNumber}")
                if (resId != 0) PixelSprite(resId, 0, 0)
                
                val charResId = getResId(state.character, "play", if (state.totalTimeSeconds.toInt() % 2 == 0) 1 else 2)
                if (charResId != 0) PixelSprite(charResId, 8, 0)
            }
            GameState.Result -> {
                val resId = getResId("num${state.givenNumber}")
                if (resId != 0) PixelSprite(resId, 0, 0)
                
                val action = if (state.gameAnswer == "higher") "right" else "left"
                val charResId = getResId(state.character, action)
                if (charResId != 0) PixelSprite(charResId, 8, 0)
                
                val secretResId = getResId("num${state.secretNumber}")
                if (secretResId != 0) PixelSprite(secretResId, 24, 0)
            }
            GameState.FinalResult -> {
                PixelSprite(com.example.nisegochi.R.drawable.face_icon, 0, 0)
                PixelSprite(com.example.nisegochi.R.drawable.empty_heart, 24, 0)
                val scoreResId = getResId("num${state.gameScore}")
                if (scoreResId != 0) PixelSprite(scoreResId, 2, 8)
                PixelSprite(com.example.nisegochi.R.drawable.vs, 8, 8)
                val lossResId = getResId("num${5 - state.gameScore}")
                if (lossResId != 0) PixelSprite(lossResId, 18, 8)
            }
            else -> {}
        }
    }
}

@Composable
fun SantaGameView(state: com.example.nisegochi.domain.PetState) {
    PixelCanvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        when (state.gameState) {
            GameState.Intro -> {
                // Scrolling bells and chimneys
                val k = 32 - state.gameAnimationCounter
                for (i in 0 until 4) {
                    val x = (i * 8 - k).coerceAtLeast(-8)
                    PixelSprite(com.example.nisegochi.R.drawable.bell_empty, x, (i % 2) * 8)
                }
                for (i in 0 until 3) {
                    val x = (10 * i + 32 - k).coerceAtMost(32)
                    PixelSprite(com.example.nisegochi.R.drawable.chimney_small, x, 6)
                }
            }
            GameState.Playing -> {
                // 3 chimneys and arrow
                PixelSprite(com.example.nisegochi.R.drawable.down_arrow, state.givenNumber * 10 + 2, 0)
                for (i in 0 until 3) {
                    PixelSprite(com.example.nisegochi.R.drawable.chimney_small, i * 10, 6)
                }
            }
            GameState.Result -> {
                // Santa going down chimney
                val k = 3 - (state.gameAnimationCounter % 2 + 1)
                if (state.gameAnimationCounter > 1) {
                    val charResId = getResId(state.character, "chimney", k)
                    if (charResId != 0) PixelSprite(charResId, 8, 0)
                } else {
                    PixelSprite(com.example.nisegochi.R.drawable.chimney_large, 8, 0)
                }
            }
            GameState.FinalResult -> {
                // Result found
                when (state.chimneyResult) {
                    "soot" -> {
                        val charResId = getResId(state.character, "idle", 1)
                        if (charResId != 0) PixelSprite(charResId, 8, 0)
                    }
                    "pet" -> {
                        PixelSprite(com.example.nisegochi.R.drawable.scale_icon, 4, 4)
                        val charResId = getResId(state.character, "idle", 2)
                        if (charResId != 0) PixelSprite(charResId, 16, 0)
                    }
                    "object" -> {
                        PixelSprite(com.example.nisegochi.R.drawable.meal_pie_1, 0, 0)
                        val charResId = getResId(state.character, "idle", 2)
                        if (charResId != 0) PixelSprite(charResId, 16, 0)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun getResId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

@Composable
fun getResId(character: String, action: String, frame: Int? = null): Int {
    val resName = if (frame != null) "${character}_${action}_$frame" else "${character}_$action"
    return getResId(resName)
}

@Composable
fun GameButton(label: String, onClick: () -> Unit) {
    androidx.compose.material3.Button(onClick = onClick) {
        Text(label)
    }
}
