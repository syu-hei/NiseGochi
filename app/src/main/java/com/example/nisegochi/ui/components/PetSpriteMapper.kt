package com.example.nisegochi.ui.components

import android.content.Context
import com.example.nisegochi.R
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.domain.GameState

data class SpriteResult(
    val drawableId: Int,
    val needsSickOverlay: Boolean = false
)

object PetSpriteMapper {
    private val resourceCache = mutableMapOf<String, Int>()

    private fun getResId(res: String, context: Context): Int {
        return resourceCache.getOrPut(res) {
            val id = context.resources.getIdentifier(res, "drawable", context.packageName)
            if (id != 0) id else R.drawable.placeholder
        }
    }

    fun getSprite(state: PetState, context: Context, frame: Int): SpriteResult {
        val character = state.character.lowercase()

        if (state.evolutionAnimationCounter > 0) {
            val scrambleFrame = (state.totalTimeSeconds * 10).toInt() % 2
            val targetChar = if (scrambleFrame == 0) character else state.nextCharacter?.lowercase() ?: character
            val res = "${targetChar}_idle_$frame"
            return SpriteResult(getResId(res, context))
        }

        if (character == "cabin") {
            val t = (state.totalTimeSeconds - state.lastEvolutionTimeSeconds).toInt()
            val res = when (state.state) {
                "hatching" -> {
                    val hatchingT = t - 10 
                    if (hatchingT <= 8) "cabin_open_door" else "santatchi_hatching"
                }
                else -> "cabin_idle_$frame"
            }
            return SpriteResult(getResId(res, context))
        }
        
        if (character == "egg") {
            val eggRes = when (state.state) {
                "hatching" -> "hatching"
                "cracking1" -> "egg_crack_1"
                "cracking2" -> "egg_crack_2"
                "cracking3" -> "egg_crack_3"
                else -> "egg_idle_$frame"
            }
            return SpriteResult(getResId(eggRes, context))
        }

        val action = when {
            state.state == "dead_ufo" || state.state == "dead_angel" -> "idle"
            state.isSleeping -> "sleep"
            state.isScolding -> "no"
            state.isHealing -> "sick"
            state.isSick -> "sick"
            state.state == "eating" -> "eat"
            state.state == "playing" -> "play"
            state.state == "happy" -> "happy"
            state.state == "unhappy" -> "unhappy"
            state.state == "no" -> "no"
            state.happy == 0 -> "unhappy"
            else -> "idle"
        }
        
        val resName = if (action == "happy" || action == "right" || action == "left") {
            "${character}_$action"
        } else {
            "${character}_${action}_$frame"
        }
        
        var id = getResId(resName, context)
        
        if (state.isSick && id == R.drawable.placeholder) {
            // Fallback to idle + sick overlay
            val idleRes = "${character}_idle_$frame"
            id = getResId(idleRes, context)
            return SpriteResult(id, needsSickOverlay = true)
        }
        
        return SpriteResult(id)
    }
}
