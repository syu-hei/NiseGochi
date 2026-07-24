package com.example.nisegochi.domain

import kotlin.math.max

object DarwinEngine {

    fun evolve(state: PetState): PetState {
        val nextChar = getEvolutionTarget(state)
        if (nextChar == state.character) return state
        
        // Apply special logic for tonmarutchi -> teen evolution
        var updatedState = state
        if (state.character == "tonmarutchi") {
            if (state.disciplineMistakes < 2) {
                updatedState = state.copy(
                    isSuperTeen = true,
                    discipline = 50, // 2 chunks out of 4? Assuming 0-100 scale
                    disciplineCallPeriod = 18.0 * 3600.0
                )
            } else {
                updatedState = state.copy(
                    isSuperTeen = false,
                    discipline = 0,
                    disciplineCallPeriod = 9.0 * 3600.0
                )
            }
        }

        val info = PetConstants.evolutionData[nextChar] ?: return updatedState
        return updatedState.copy(
            character = nextChar,
            hungryPeriod = info.hungryPeriod,
            happyPeriod = info.happyPeriod,
            poopPeriod = info.poopPeriod,
            disciplineCallPeriod = if (state.character == "tonmarutchi") updatedState.disciplineCallPeriod else info.disciplineCallPeriod,
            sleepingHour = info.sleepingHour,
            wakingHour = info.wakingHour,
            yOffset = info.yOffset,
            idealWeight = info.idealWeight,
            walks = info.walks,
            timeSinceHungryChanged = 0.0,
            timeSinceHappyChanged = 0.0,
            timeSinceLastPooped = 0.0,
            timeForDisciplineCall = updatedState.totalTimeSeconds + (if (state.character == "tonmarutchi") updatedState.disciplineCallPeriod else info.disciplineCallPeriod),
            weight = max(updatedState.weight, info.idealWeight),
            lastEvolutionTimeSeconds = updatedState.totalTimeSeconds
        )
    }

    fun getEvolutionTarget(state: PetState): String {
        val CM = state.careMisses
        val DM = state.disciplineMistakes
        val isSuperTeen = state.isSuperTeen

        return when (state.character) {
            "egg" -> "babytchi"
            "babytchi" -> "tonmarutchi"
            "tonmarutchi" -> {
                if (CM < 3) "tongaritchi" else "hashitamatchi"
            }
            "tongaritchi" -> {
                if (isSuperTeen) {
                    if (CM < 3) {
                        when {
                            DM == 0 -> "mimitchi"
                            DM == 1 -> "pochitchi"
                            else -> "zuccitchi" // Map maskutchi to zuccitchi
                        }
                    } else {
                        when {
                            DM < 2 -> "hashizotchi"
                            DM < 4 -> "kusatchi"
                            else -> "takotchi"
                        }
                    }
                } else {
                    if (CM < 4) {
                        "zuccitchi" // Map maskutchi to zuccitchi
                    } else {
                        if (DM <= 7) "kusatchi" else "takotchi"
                    }
                }
            }
            "hashitamatchi" -> {
                if (isSuperTeen) {
                    when {
                        DM < 2 -> "hashizotchi"
                        DM == 2 -> "kusatchi"
                        else -> "takotchi"
                    }
                } else {
                    if (DM <= 5) "kusatchi" else "takotchi"
                }
            }
            "zuccitchi" -> if (isSuperTeen) "zatchi" else state.character
            else -> state.character
        }
    }
}
