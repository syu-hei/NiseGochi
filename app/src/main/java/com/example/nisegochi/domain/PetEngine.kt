package com.example.nisegochi.domain

import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object PetEngine {

    fun tick(state: PetState): PetState {
        if (state.isPaused) return state
        
        val timeIncrement = if (state.lifespanBonus) 0.5 else 1.0
        var nextState = state.copy(totalTimeSeconds = max(0.0, state.totalTimeSeconds + timeIncrement))

        // Animation Counters
        if (nextState.evolutionAnimationCounter > 0) {
            nextState = nextState.copy(evolutionAnimationCounter = nextState.evolutionAnimationCounter - 1)
            if (nextState.evolutionAnimationCounter == 0) {
                nextState.nextCharacter?.let { 
                    nextState = performEvolution(nextState, it)
                }
            }
            return nextState
        }

        if (nextState.deathAnimationCounter > 0) {
            nextState = nextState.copy(deathAnimationCounter = nextState.deathAnimationCounter - 1)
            return nextState
        }

        if (!nextState.isAlive) return nextState

        // Reset metabolism if expired
        if ((nextState.metabolismFactor > 1.0) && nextState.totalTimeSeconds >= nextState.metabolismEndTime) {
            nextState = nextState.copy(metabolismFactor = 1.0)
        }

        if (nextState.character == "egg") {
            return updateEgg(nextState)
        }
        
        if (nextState.character == "cabin") {
            return updateCabin(nextState)
        }

        if (nextState.gameState != GameState.None) {
            return updateGame(nextState)
        }

        if (!nextState.isSleeping) {
            nextState = updateHunger(nextState)
            nextState = updateHappiness(nextState)
        }

        nextState = updatePoop(nextState)
        nextState = updateSickness(nextState)
        nextState = updateSleep(nextState)
        nextState = updateDiscipline(nextState)
        nextState = updateEvolution(nextState)
        nextState = checkCalls(nextState)
        nextState = updateDeath(nextState)

        return nextState
    }

    private fun checkCalls(state: PetState): PetState {
        val needsCall = state.stomach == 0 || state.happy == 0 || state.needsDiscipline
        
        var s = state
        if (needsCall && !s.isCalling) {
            s = s.copy(isCalling = true, callStartTime = s.totalTimeSeconds)
        } else if (!needsCall && s.isCalling) {
            s = s.copy(isCalling = false, callStartTime = null)
        }
        
        // Handle 15-minute timeout
        if (s.isCalling && s.callStartTime != null) {
            if (s.totalTimeSeconds - s.callStartTime >= 900.0) {
                s = if (s.needsDiscipline) {
                    s.copy(
                        disciplineMistakes = s.disciplineMistakes + 1,
                        needsDiscipline = false,
                        isCalling = false,
                        callStartTime = null
                    )
                } else {
                    s.copy(
                        careMisses = s.careMisses + 1,
                        isCalling = false,
                        callStartTime = null
                    )
                }
            }
        }
        return s
    }

    private fun updateGame(state: PetState): PetState {
        var s = state
        if (s.gameAnimationCounter > 0) {
            s = s.copy(gameAnimationCounter = s.gameAnimationCounter - 1)
        }

        if (s.gameAnimationCounter == 0) {
            when (s.gameState) {
                GameState.Intro -> {
                    s = s.copy(gameState = GameState.Playing)
                }
                GameState.Result -> {
                    if (s.gameType == "GiftFinding") {
                        s = s.copy(gameState = GameState.FinalResult, gameAnimationCounter = 8)
                    } else {
                        s = endRound(s)
                    }
                }
                GameState.FinalResult -> {
                    s = finishGame(s)
                }
                else -> {}
            }
        }
        return s
    }

    private fun endRound(state: PetState): PetState {
        val s = state
        val won = (s.gameAnswer == "higher" && s.secretNumber > s.givenNumber) ||
                  (s.gameAnswer == "lower" && s.secretNumber < s.givenNumber)
        
        val newScore = if (won) s.gameScore + 1 else s.gameScore
        val newRound = s.gameRound + 1
        
        return if (newRound < 5) {
            s.copy(
                gameScore = newScore,
                gameRound = newRound,
                gameState = GameState.Playing,
                gameAnimationCounter = 0,
                givenNumber = Random.nextInt(0, 10),
                secretNumber = Random.nextInt(0, 10)
            )
        } else {
            s.copy(
                gameScore = newScore,
                gameRound = newRound,
                gameState = GameState.FinalResult,
                gameAnimationCounter = 8
            )
        }
    }

    private fun finishGame(state: PetState): PetState {
        var s = state
        val winGame = if (s.gameType == "GiftFinding") {
            s.chimneyResult != "soot"
        } else {
            s.gameScore >= 3
        }
        
        val alreadyFullHappy = s.happy == 4
        s = if (winGame) {
            val newHappy = min(s.happy + 1, 4)
            var nextMetaFactor = s.metabolismFactor
            var nextMetaEndTime = s.metabolismEndTime
            var nextPantry = s.pantry
            
            if (s.gameType == "GiftFinding" && s.chimneyResult == "object") {
                val newItem = PetConstants.PANTRY_ITEMS.random()
                nextPantry = s.pantry + newItem
            }

            if (alreadyFullHappy) {
                nextMetaFactor = 1.5 // Active pet: faster drain
                nextMetaEndTime = s.totalTimeSeconds + 3600.0
            }

            s.copy(
                happy = newHappy,
                timeSinceHappyChanged = 0.0,
                timeSinceHappyEmpty = 0.0,
                weight = max(s.weight - 1, s.idealWeight),
                metabolismFactor = nextMetaFactor,
                metabolismEndTime = nextMetaEndTime,
                pantry = nextPantry
            )
        } else {
            s.copy(weight = max(s.weight - 1, s.idealWeight))
        }
        
        return s.copy(
            gameState = GameState.None,
            gameRound = 0,
            gameScore = 0,
            gameType = "HigherLower" // Reset to default
        )
    }

    private fun updateEgg(state: PetState): PetState {
        val t = state.totalTimeSeconds
        return when {
            t == 5.0 -> state.copy(state = "hatching")
            state.state == "hatching" && t == 7.0 -> state.copy(state = "cracking1")
            state.state == "cracking1" && t == 8.0 -> state.copy(state = "cracking2")
            state.state == "cracking2" && t == 9.0 -> state.copy(state = "cracking3")
            state.state == "cracking3" && t >= 10.0 -> {
                state.copy(
                    gameState = GameState.Naming
                )
            }
            else -> state
        }
    }

    private fun updateCabin(state: PetState): PetState {
        // Simple hatching for cabin
        val t = state.totalTimeSeconds - state.lastEvolutionTimeSeconds
        return when {
            state.state == "idle" && t >= 10.0 -> state.copy(state = "hatching")
            state.state == "hatching" && t >= 20.0 -> {
                val info = PetConstants.evolutionData["santatchi"]!!
                state.copy(
                    character = "santatchi",
                    state = "idle",
                    hungryPeriod = info.hungryPeriod,
                    happyPeriod = info.happyPeriod,
                    poopPeriod = info.poopPeriod,
                    disciplineCallPeriod = info.disciplineCallPeriod,
                    sleepingHour = info.sleepingHour,
                    wakingHour = info.wakingHour,
                    yOffset = info.yOffset,
                    idealWeight = info.idealWeight,
                    walks = info.walks,
                    isCalling = true,
                    lastEvolutionTimeSeconds = state.totalTimeSeconds
                ).computeSleepWakeTimes()
            }
            else -> state
        }
    }

    private fun updateHunger(state: PetState): PetState {
        var s = state
        if (s.stomach == 0) {
            s = s.copy(timeSinceHungryEmpty = s.timeSinceHungryEmpty + 1)
            // Timeout logic moved to checkCalls
            if (s.timeSinceHungryEmpty >= 24.0 * 3600.0) {
                return s.copy(isAlive = false, state = "dead1")
            }
        }

        val effectiveTick = 1.0 * s.metabolismFactor
        s = s.copy(timeSinceHungryChanged = s.timeSinceHungryChanged + effectiveTick)
        if (s.timeSinceHungryChanged >= s.hungryPeriod) {
            val newStomach = (s.stomach - 1).coerceIn(0, 4)
            s = s.copy(
                stomach = newStomach,
                timeSinceHungryChanged = 0.0
            )
        }
        return s
    }

    private fun updateHappiness(state: PetState): PetState {
        var s = state
        if (s.happy == 0) {
            s = s.copy(timeSinceHappyEmpty = s.timeSinceHappyEmpty + 1)
            // Timeout logic moved to checkCalls
            if (s.timeSinceHappyEmpty >= 24.0 * 3600.0) {
                return s.copy(isAlive = false, state = "dead1")
            }
        }

        val effectiveTick = 1.0 * s.metabolismFactor
        s = s.copy(timeSinceHappyChanged = s.timeSinceHappyChanged + effectiveTick)
        if (s.timeSinceHappyChanged >= s.happyPeriod) {
            val newHappy = (s.happy - 1).coerceIn(0, 4)
            s = s.copy(
                happy = newHappy,
                timeSinceHappyChanged = 0.0
            )
        }
        return s
    }

    private fun updateSleep(state: PetState): PetState {
        var s = state
        if (s.character == "babytchi") {
            if (s.totalTimeSeconds == 2400.0) {
                s = s.copy(isSleeping = true, state = "idle")
            }
            if (s.totalTimeSeconds == 2700.0) {
                s = s.copy(isSleeping = false, timeSinceNeedsLightsOff = 0.0, isLightOn = true, age = s.age + 1)
            }
        } else {
            if (s.totalTimeSeconds == s.timeToSleep) {
                s = s.copy(isSleeping = true, timeToSleep = s.timeToSleep + 24 * 3600)
            } else if (s.totalTimeSeconds == s.timeToWake) {
                s = s.copy(isSleeping = false, isLightOn = true, timeSinceNeedsLightsOff = 0.0, timeToWake = s.timeToWake + 24 * 3600, age = s.age + 1)
            }
        }

        if (s.isSleeping) {
            if (s.isLightOn) {
                // Lights ON while sleeping: wake up eventually or get sick
                s = s.copy(timeSinceNeedsLightsOff = s.timeSinceNeedsLightsOff + 1)
                if (s.timeSinceNeedsLightsOff >= 15.0 * 60.0) {
                    // Force wake up or get sick
                    if (Random.nextDouble() < 0.01) { // 1% chance per tick to wake up early or get sick
                         s = s.copy(isSleeping = false, isSick = true, timeSinceNeedsLightsOff = 0.0)
                    }
                    if (s.timeSinceNeedsLightsOff % 300 == 0.0) {
                        s = s.copy(careMisses = s.careMisses + 1)
                    }
                }
            } else {
                // Lights OFF: sleep peacefully
                s = s.copy(timeSinceNeedsLightsOff = 0.0)
            }
        }
        return s
    }

    private fun updatePoop(state: PetState): PetState {
        var s = state
        if (s.character.equals("babytchi", ignoreCase = true)) {
            if (s.totalTimeSeconds == 20.0 || s.totalTimeSeconds == 2705.0) {
                s = s.copy(isDirty = true, timeSinceLastPooped = 0.0)
            }
        } else {
            if (!s.isSleeping && s.state != "Egg") {
                s = s.copy(timeSinceLastPooped = s.timeSinceLastPooped + 1)
                if (s.timeSinceLastPooped >= s.poopPeriod && s.poopPeriod > 0) {
                    s = s.copy(isDirty = true, timeSinceLastPooped = 0.0)
                }
            }
        }

        if (s.isDirty) {
            s = s.copy(timeSinceDirty = s.timeSinceDirty + 1)
            if (s.timeSinceDirty == 15.0 * 60.0) {
                s = s.copy(careMisses = s.careMisses + 1)
            }
            if (s.timeSinceDirty == 12.0 * 3600.0) {
                s = s.copy(isSick = true)
            }
        }
        return s
    }

    private fun updateSickness(state: PetState): PetState {
        var s = state
        if (s.totalTimeSeconds == 1980.0) {
            s = s.copy(isSick = true, state = "idle")
        }

        // Sickness due to age
        if (s.totalTimeSeconds == s.timeToGetSickFromAge) {
            s = s.copy(
                isSick = true,
                timeToGetSickFromAge = s.timeToGetSickFromAge + 72 * 3600
            )
            if (s.totalTimeSeconds > 5 * 24 * 3600) {
                s = s.decreasePeriods()
            }
        }

        // Sickness due to weight
        if (s.weight <= s.idealWeight) {
            s = s.copy(malnutritionTimer = s.malnutritionTimer + 1.0)
            if (s.malnutritionTimer >= 48 * 3600) {
                s = s.copy(isSick = true, malnutritionTimer = 0.0)
            }
        } else {
            s = s.copy(malnutritionTimer = 0.0)
        }

        if (s.weight >= s.idealWeight + 20) {
            s = s.copy(obesityTimer = s.obesityTimer + 1.0)
            if (s.obesityTimer >= 3600) { // 1 hour of obesity triggers sickness
                s = s.copy(isSick = true, obesityTimer = 0.0)
            }
        } else {
            s = s.copy(obesityTimer = 0.0)
        }

        if (s.totalTimeSeconds % 3600 == 0.0) {
            val weightDiff = s.weight - s.idealWeight
            if (Random.nextDouble() < (weightDiff / 100.0)) {
                s = s.copy(isSick = true)
                s = s.decreasePeriods()
            }
        }

        if (s.isSick && !s.isSleeping) {
            s = s.copy(timeSinceSick = s.timeSinceSick + 1)
            if (s.timeSinceSick == 15.0 * 60.0) {
                s = s.copy(careMisses = s.careMisses + 1)
            } else if (s.timeSinceSick == 12.0 * 3600.0) {
                s = s.copy(isAlive = false, state = "dead1")
            }
        }
        return s
    }

    private fun PetState.decreasePeriods(): PetState {
        return this.copy(
            hungryPeriod = max(this.hungryPeriod - 300, 10 * 60),
            happyPeriod = max(this.happyPeriod - 300, 10 * 60),
            poopPeriod = max(this.poopPeriod - 300, 10 * 60)
        )
    }

    private fun updateDiscipline(state: PetState): PetState {
        var s = state
        if (s.totalTimeSeconds == s.timeForDisciplineCall) {
            if (!s.isSleeping) {
                s = s.copy(needsDiscipline = true, timeSinceNeedsDiscipline = 0.0)
            }
            s = s.copy(timeForDisciplineCall = s.timeForDisciplineCall + s.disciplineCallPeriod)
        }

        if (s.needsDiscipline) {
            s = s.copy(timeSinceNeedsDiscipline = s.timeSinceNeedsDiscipline + 1)
            // Timeout logic moved to checkCalls
        }

        // Discipline decay for adults
        if (isAdult(s.character)) {
            s = s.copy(disciplineDecayTimer = s.disciplineDecayTimer + 1.0)
            if (s.disciplineDecayTimer >= 24 * 3600) {
                s = s.copy(
                    discipline = (s.discipline - 10).coerceIn(0, 100),
                    disciplineDecayTimer = 0.0
                )
            }
        }

        return s
    }

    private fun isAdult(character: String): Boolean {
        val adults = listOf("mimitchi", "pochitchi", "zuccitchi", "hashizotchi", "takotchi", "kusatchi", "zatchi", "santatchi")
        return adults.contains(character.lowercase())
    }

    private fun updateEvolution(state: PetState): PetState {
        var s = state
        val t = s.totalTimeSeconds
        val shouldEvolve = when {
            t == 65.0 * 60.0 -> true
            t == 2.0 * 24 * 3600 -> true
            t == 5.0 * 24 * 3600 -> true
            t == 8.0 * 24 * 3600 && s.character.equals("zuccitchi", ignoreCase = true) -> true
            else -> false
        }

        if (shouldEvolve) {
            val nextChar = DarwinEngine.getEvolutionTarget(s)
            if (nextChar != s.character) {
                return s.copy(
                    evolutionAnimationCounter = 5, // 5 seconds of scramble
                    nextCharacter = nextChar,
                    state = "evolving"
                )
            }
        }
        return s
    }

    private fun performEvolution(state: PetState, nextChar: String): PetState {
        val info = PetConstants.evolutionData[nextChar] ?: return state
        val isNowAdult = isAdult(nextChar)
        val hasBonus = isNowAdult && state.careMisses == 0

        return state.copy(
            character = nextChar,
            state = "idle",
            hungryPeriod = info.hungryPeriod,
            happyPeriod = info.happyPeriod,
            poopPeriod = info.poopPeriod,
            disciplineCallPeriod = info.disciplineCallPeriod,
            sleepingHour = info.sleepingHour,
            wakingHour = info.wakingHour,
            yOffset = info.yOffset,
            idealWeight = info.idealWeight,
            walks = info.walks,
            timeSinceHungryChanged = 0.0,
            timeSinceHappyChanged = 0.0,
            timeSinceLastPooped = 0.0,
            timeForDisciplineCall = state.totalTimeSeconds + info.disciplineCallPeriod,
            weight = max(state.weight, info.idealWeight),
            lastEvolutionTimeSeconds = state.totalTimeSeconds,
            nextCharacter = null,
            evolutionAnimationCounter = 0,
            lifespanBonus = state.lifespanBonus || hasBonus,
            malnutritionTimer = 0.0,
            obesityTimer = 0.0,
            disciplineDecayTimer = 0.0
        ).computeSleepWakeTimes()
    }

    fun PetState.computeSleepWakeTimes(): PetState {
        // Simplified logic for computing sleep/wake times based on 1970 birth time
        // In a real app, we'd use Calendar or java.time
        val currentTimeMillis = this.birthTimeMillis + (this.totalTimeSeconds * 1000).toLong()
        val now = Date(currentTimeMillis)
        
        val calendar = Calendar.getInstance()
        calendar.time = now
        
        calendar[Calendar.HOUR_OF_DAY] = this.sleepingHour
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        var sleepTimeMillis = calendar.timeInMillis
        if (sleepTimeMillis <= currentTimeMillis) {
            sleepTimeMillis += 24L * 3600L * 1000L
        }
        val timeToSleep = (sleepTimeMillis - this.birthTimeMillis) / 1000.0

        calendar[Calendar.HOUR_OF_DAY] = this.wakingHour
        var wakeTimeMillis = calendar.timeInMillis
        if (wakeTimeMillis <= currentTimeMillis) {
            wakeTimeMillis += 24L * 3600L * 1000L
        }
        val timeToWake = (wakeTimeMillis - this.birthTimeMillis) / 1000.0

        return this.copy(
            timeToSleep = timeToSleep,
            timeToWake = timeToWake
        )
    }

    private fun updateDeath(state: PetState): PetState {
        if (state.totalTimeSeconds >= 25.0 * 24.0 * 3600.0 || state.careMisses >= 50) {
            val isSanta = state.character.startsWith("santa")
            return state.copy(
                isAlive = false,
                state = if (isSanta) "dead_angel" else "dead_ufo",
                deathAnimationCounter = 10
            )
        }
        return state
    }

    fun feed(state: PetState, isMeal: Boolean): PetState {
        var s = if (isMeal) {
            state.copy(
                stomach = min(state.stomach + 1, 4),
                weight = state.weight + 1,
                state = "eating",
                timeSinceHungryChanged = 0.0,
                timeSinceHungryEmpty = 0.0
            )
        } else {
            state.copy(
                happy = min(state.happy + 1, 4),
                weight = state.weight + 2,
                state = "eating",
                timeSinceHappyChanged = 0.0,
                timeSinceHappyEmpty = 0.0
            )
        }
        return checkCalls(s)
    }
}
