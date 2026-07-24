package com.example.nisegochi.domain

import java.io.Serializable

enum class GameState {
    None, Intro, Playing, Result, FinalResult, Naming
}

data class PetState(
    val character: String = "egg",
    val name: String = "",
    val state: String = "Egg",
    val stomach: Int = 0,
    val happy: Int = 0,
    val weight: Int = 5,
    val age: Int = 0,
    val discipline: Int = 0,
    val isAlive: Boolean = true,
    val isSleeping: Boolean = false,
    val isDirty: Boolean = false,
    val isSick: Boolean = false,
    val isCalling: Boolean = false,
    val needsDiscipline: Boolean = false,
    val isLightOn: Boolean = true,
    val walks: Boolean = true,
    val isSuperTeen: Boolean = false,
    val isPaused: Boolean = false,
    val isScolding: Boolean = false,
    val isHealing: Boolean = false,
    val petName: String = "",
    val totalTimeSeconds: Double = 0.0,
    val birthTimeMillis: Long = System.currentTimeMillis(),
    val lifespanBonus: Boolean = false,
    val isDebugMode: Boolean = false,
    
    // Internal counters/timers
    val timeSinceHungryChanged: Double = 0.0,
    val timeSinceHungryEmpty: Double = 0.0,
    val timeSinceHappyChanged: Double = 0.0,
    val timeSinceHappyEmpty: Double = 0.0,
    val timeSinceLastPooped: Double = 0.0,
    val timeSinceDirty: Double = 0.0,
    val timeSinceSick: Double = 0.0,
    val timeSinceNeedsDiscipline: Double = 0.0,
    val timeSinceNeedsLightsOff: Double = 0.0,
    val timeToGetSickFromAge: Double = 42.0 * 3600.0,
    val timeForDisciplineCall: Double = 0.0,
    val timeToSleep: Double = 0.0,
    val timeToWake: Double = 0.0,
    val malnutritionTimer: Double = 0.0,
    val obesityTimer: Double = 0.0,
    val disciplineDecayTimer: Double = 0.0,
    
    // Stats
    val careMisses: Int = 0,
    val disciplineMistakes: Int = 0,
    val cakesEaten: Int = 0,
    
    // Hardcore Mechanics
    val metabolismFactor: Double = 1.0,
    val metabolismEndTime: Double = 0.0,
    val callStartTime: Double? = null,
    val disciplineCallStartTime: Double? = null,
    
    // Game State
    val gameState: GameState = GameState.None,
    val gameRound: Int = 0,
    val gameScore: Int = 0,
    val givenNumber: Int = 0,
    val secretNumber: Int = 0,
    val gameAnswer: String? = null,
    val gameAnimationCounter: Int = 0,
    val gameType: String = "HigherLower",
    val chimneyResult: String = "",
    val lastEvolutionTimeSeconds: Double = 0.0,
    val evolutionAnimationCounter: Int = 0,
    val nextCharacter: String? = null,
    val deathAnimationCounter: Int = 0,
    val pantry: List<String> = emptyList(),
    val selectedPantryIndex: Int = 0,

    // Configuration from Darwin
    val hungryPeriod: Int = 180,
    val happyPeriod: Int = 240,
    val poopPeriod: Int = 0,
    val disciplineCallPeriod: Double = 5.5 * 3600.0,
    val sleepingHour: Int = 20,
    val wakingHour: Int = 9,
    val idealWeight: Int = 5,
    val yOffset: Int = 0,
    val isMuted: Boolean = false
) : Serializable
