package com.example.nisegochi.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nisegochi.R
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.domain.GameState
import com.example.nisegochi.ui.components.IconBar
import com.example.nisegochi.ui.components.PixelCanvas
import com.example.nisegochi.ui.components.PixelSprite
import com.example.nisegochi.ui.viewmodel.PetViewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import com.example.nisegochi.ui.components.PetSpriteMapper
import com.example.nisegochi.ui.components.SpriteResult

@Composable
fun MainScreen(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsState()
    val highlightedIcon by viewModel.highlightedIcon.collectAsState()
    
    val topIcons = listOf(
        R.drawable.food_icon,
        R.drawable.lights_icon,
        R.drawable.game_icon,
        R.drawable.medicine_icon
    )
    
    val bottomIcons = listOf(
        R.drawable.toilet_icon,
        R.drawable.stats_icon,
        R.drawable.training_icon,
        R.drawable.attention_icon
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBar(icons = topIcons, selectedIndex = highlightedIcon, startIndex = 1)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(contentAlignment = Alignment.Center) {
            PixelCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                if (petState.isMuted) {
                    PixelSprite(drawableId = R.drawable.mute_icon, x = 24, y = 0)
                }
                
                val spriteResult = getCharacterSpriteResult(petState)
                val charSprite = spriteResult.drawableId
                
                if (charSprite != 0) {
                    var x = 8
                    var y = 0
                    
                    // Walking Jitter
                    if (petState.isAlive && !petState.isSleeping && petState.gameState == GameState.None && 
                        petState.state == "idle" && petState.walks) {
                        val infiniteTransition = rememberInfiniteTransition(label = "JitterTransition")
                        val jitterX by infiniteTransition.animateFloat(
                            initialValue = -1f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "JitterX"
                        )
                        x += (jitterX * 4).toInt()
                    }

                    // Death Animation logic
                    if (petState.deathAnimationCounter > 0) {
                        val progress = 10 - petState.deathAnimationCounter
                        y = -progress // Moving up
                        
                        if (petState.state == "dead_ufo") {
                            // UFO appears at the top
                            PixelSprite(drawableId = R.drawable.ufo, x = 8, y = -12 + progress)
                            
                            // Flickering Stars around the UFO
                            val starFlicker = (petState.deathAnimationCounter % 2 == 0)
                            if (starFlicker) {
                                PixelSprite(drawableId = R.drawable.star1, x = 0, y = -10 + progress)
                                PixelSprite(drawableId = R.drawable.star2, x = 24, y = -14 + progress)
                                PixelSprite(drawableId = R.drawable.star3, x = 12, y = -18 + progress)
                            } else {
                                PixelSprite(drawableId = R.drawable.star2, x = 4, y = -16 + progress)
                                PixelSprite(drawableId = R.drawable.star3, x = 20, y = -10 + progress)
                                PixelSprite(drawableId = R.drawable.star1, x = 8, y = -20 + progress)
                            }
                        }
                    }

                    PixelSprite(drawableId = charSprite, x = x, y = y + petState.yOffset)
                    
                    if (spriteResult.needsSickOverlay || petState.isHealing) {
                        val overlayId = if (petState.isHealing) R.drawable.medicine_icon else R.drawable.skull
                        PixelSprite(drawableId = overlayId, x = x + 16, y = y + petState.yOffset)
                    }

                    if (petState.state == "dead_angel") {
                        // Halo above the head
                        PixelSprite(drawableId = R.drawable.angel_halo, x = x, y = y - 4)
                    }

                    if (petState.happy == 0 && petState.isAlive && !petState.isSleeping && petState.gameState == GameState.None) {
                        val cloudTransition = rememberInfiniteTransition(label = "CloudTransition")
                        val cloudFrame by cloudTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "CloudFrame"
                        )
                        val cloudId = if (cloudFrame < 0.5f) R.drawable.unhappy_cloud_1 else R.drawable.unhappy_cloud_2
                        PixelSprite(drawableId = cloudId, x = x + 4, y = y - 10 + petState.yOffset)
                    }
                }
                
                if (petState.isDirty && petState.isAlive) {
                    val poopTransition = rememberInfiniteTransition(label = "PoopTransition")
                    val poopFrame by poopTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "PoopFrame"
                    )
                    val poopId = if (poopFrame < 0.5f) R.drawable.poop_1 else R.drawable.poop_2
                    PixelSprite(drawableId = poopId, x = 24, y = 8)
                }

                if (petState.isSleeping && petState.isAlive) {
                    val sleepTransition = rememberInfiniteTransition(label = "SleepTransition")
                    val zFrame by sleepTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "ZFrame"
                    )
                    val zId = if (zFrame < 0.5f) {
                        if (petState.isLightOn) R.drawable.z1 else R.drawable.z1dark
                    } else {
                        if (petState.isLightOn) R.drawable.z2 else R.drawable.z2dark
                    }
                    PixelSprite(drawableId = zId, x = 24, y = 4)
                }

                if (petState.state == "cleaning") {
                    val infiniteTransition = rememberInfiniteTransition(label = "ShowerTransition")
                    val showerX by infiniteTransition.animateFloat(
                        initialValue = 32f,
                        targetValue = -32f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ShowerX"
                    )
                    
                    PixelSprite(drawableId = R.drawable.shower, x = showerX.toInt(), y = 0)
                    PixelSprite(drawableId = R.drawable.shower, x = (showerX + 32).toInt(), y = 0)
                }
            }

            if (!petState.isLightOn) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 8.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                )
            }

            if (petState.isPaused) {
                Text(
                    text = "PAUSE",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        IconBar(
            icons = bottomIcons, 
            selectedIndex = highlightedIcon, 
            startIndex = 5,
            attentionActive = petState.isCalling || petState.needsDiscipline || petState.isSick || petState.isDirty || petState.isScolding
        )
    }
}

@Composable
fun getCharacterSpriteResult(state: PetState): SpriteResult {
    val context = LocalContext.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "SpriteAnimation")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimationProgress"
    )
    val frame = if (animationProgress < 0.5f) 1 else 2
    
    return remember(state, frame) {
        PetSpriteMapper.getSprite(state, context, frame)
    }
}
