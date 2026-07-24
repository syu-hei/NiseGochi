package com.example.nisegochi.ui.audio

import android.content.Context
import android.media.MediaPlayer
import java.util.HashMap

object AudioManager {
    private val mediaPlayerMap = HashMap<String, MediaPlayer>()
    var isMuted: Boolean = false
    
    private val soundList = arrayOf(
        "bad_sound", "call", "discipline_sound", "display_results_sound",
        "evolve_sound", "flip_sound", "game_begin", "good_sound",
        "hatching_sound", "reset_sound", "small_beep",
        "santa_call_sound", "santa_small_beep", "santa_validate_sound", "cabin_exit",
        "death_march", "fanfare", "santa_exit"
    )

    fun loadSounds(context: Context) {
        val pkg = context.packageName
        for (soundName in soundList) {
            val resId = context.resources.getIdentifier(soundName, "raw", pkg)
            if (resId != 0) {
                val mp = MediaPlayer.create(context, resId)
                if (mp != null) {
                    mediaPlayerMap[soundName] = mp
                }
            }
        }
    }

    fun playSound(soundName: String) {
        if (isMuted) return
        val mp = mediaPlayerMap[soundName]
        if (mp != null) {
            if (mp.isPlaying) {
                mp.stop()
                mp.prepare()
            }
            mp.start()
        }
    }
    
    fun release() {
        for (mp in mediaPlayerMap.values) {
            mp.release()
        }
        mediaPlayerMap.clear()
    }
}
