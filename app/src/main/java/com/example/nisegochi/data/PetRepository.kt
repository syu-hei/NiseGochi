package com.example.nisegochi.data

import com.example.nisegochi.data.persistence.PetDao
import com.example.nisegochi.data.persistence.toDomain
import com.example.nisegochi.data.persistence.toEntity
import com.example.nisegochi.domain.PetEngine
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.domain.GameState
import com.example.nisegochi.domain.DarwinEngine
import com.example.nisegochi.domain.PetConstants
import com.example.nisegochi.ui.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetRepository(
    private val petDao: PetDao,
    private val notificationHelper: NotificationHelper? = null
) {
    private val _petState = MutableStateFlow(PetState())
    val petState: StateFlow<PetState> = _petState.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastSaveTime = System.currentTimeMillis()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        scope.launch {
            loadFromDb()
            _isLoaded.value = true
        }
    }

    private suspend fun loadFromDb() {
        val entity = petDao.getPetState()
        if (entity != null) {
            val loadedState = entity.toDomain()
            val lastSaved = entity.lastUpdatedTimestamp
            val now = System.currentTimeMillis()
            
            // Catch up logic
            val secondsPassed = (now - lastSaved) / 1000
            var caughtUpState = loadedState
            
            // Limit catch up to 24 hours to avoid infinite loops or crashes if the time jump is huge
            val maxCatchUpSeconds = 24 * 3600L
            val actualSecondsToCatchUp = secondsPassed.coerceIn(0, maxCatchUpSeconds)
            
            if (!loadedState.isPaused && actualSecondsToCatchUp > 0) {
                withContext(Dispatchers.Default) {
                    for (i in 1..actualSecondsToCatchUp) {
                        caughtUpState = PetEngine.tick(caughtUpState)
                        if (!caughtUpState.isAlive) break
                    }
                }
            }
            
            // Ensure selectedPantryIndex is valid
            val clampedIndex = if (caughtUpState.pantry.isEmpty()) 0 
                               else caughtUpState.selectedPantryIndex.coerceIn(0, caughtUpState.pantry.size - 1)
            caughtUpState = caughtUpState.copy(selectedPantryIndex = clampedIndex)
            
            _petState.value = caughtUpState
        }
    }

    fun update() {
        _petState.update { currentState ->
            PetEngine.tick(currentState)
        }
        
        // Save to DB periodically (every 10 seconds or if state changed significantly)
        val now = System.currentTimeMillis()
        if (now - lastSaveTime > 10000) {
            saveToDb()
            lastSaveTime = now
        }
    }

    private fun saveToDb() {
        scope.launch {
            petDao.insertPetState(_petState.value.toEntity())
        }
    }

    fun reset() {
        _petState.value = PetState()
        notificationHelper?.cancelAllNotifications()
        saveToDb()
    }

    fun toggleMute() {
        _petState.update { it.copy(isMuted = !it.isMuted) }
        saveToDb()
    }

    fun toggleLights() {
        _petState.update { it.copy(isLightOn = !it.isLightOn) }
        saveToDb()
    }

    fun adjustClock(seconds: Long) {
        _petState.update { 
            val next = it.copy(totalTimeSeconds = it.totalTimeSeconds + seconds)
            with(PetEngine) { next.computeSleepWakeTimes() }
        }
        saveToDb()
    }
    
    fun feed(isMeal: Boolean) {
        _petState.update { PetEngine.feed(it, isMeal) }
        scope.launch {
            kotlinx.coroutines.delay(2000)
            _petState.update { if (it.state == "eating") it.copy(state = "idle") else it }
        }
        saveToDb()
    }
    
    fun play() {
        _petState.update { it.copy(happy = (it.happy + 1).coerceAtMost(4), weight = it.weight + 1, state = "playing") }
        scope.launch {
            kotlinx.coroutines.delay(2000)
            _petState.update { if (it.state == "playing") it.copy(state = "idle") else it }
        }
        saveToDb()
    }
    
    fun clean() {
        _petState.update { it.copy(isDirty = false, timeSinceDirty = 0.0, state = "cleaning") }
        scope.launch {
            kotlinx.coroutines.delay(3000)
            _petState.update { if (it.state == "cleaning") it.copy(state = "idle") else it }
        }
        saveToDb()
    }
    
    fun heal() {
        _petState.update { it.copy(isHealing = true) }
        scope.launch {
            kotlinx.coroutines.delay(2000)
            _petState.update { it.copy(isSick = false, isHealing = false, timeSinceSick = 0.0) }
        }
        saveToDb()
    }
    
    fun cyclePantrySelection() {
        _petState.update { 
            if (it.pantry.isEmpty()) it.copy(selectedPantryIndex = 0)
            else it.copy(selectedPantryIndex = (it.selectedPantryIndex + 1) % it.pantry.size)
        }
    }

    fun usePantryItem() {
        _petState.update { state ->
            if (state.pantry.isEmpty()) return@update state
            val item = state.pantry.getOrNull(state.selectedPantryIndex) ?: return@update state
            val newPantry = state.pantry.filterIndexed { index, _ -> index != state.selectedPantryIndex }
            
            // Handle item effect
            var nextState = state.copy(
                pantry = newPantry,
                selectedPantryIndex = if (newPantry.isEmpty()) 0 else state.selectedPantryIndex % newPantry.size
            )
            
            nextState = when (item) {
                "meal_pie_1" -> PetEngine.feed(nextState, isMeal = true)
                "santa_cake_1" -> PetEngine.feed(nextState, isMeal = false)
                "full_heart" -> nextState.copy(happy = (nextState.happy + 2).coerceAtMost(4))
                "bell_icon", "game_icon" -> nextState.copy(happy = (nextState.happy + 1).coerceAtMost(4), state = "playing")
                else -> nextState
            }
            nextState
        }
        saveToDb()
    }
    
    fun startGame() {
        _petState.update { 
            val isSanta = it.character.startsWith("santa")
            it.copy(
                gameState = GameState.Intro,
                gameAnimationCounter = if (isSanta) 32 else 6,
                gameRound = 0,
                gameScore = 0,
                gameType = if (isSanta) "GiftFinding" else "HigherLower",
                givenNumber = if (isSanta) 1 else kotlin.random.Random.nextInt(0, 10),
                secretNumber = kotlin.random.Random.nextInt(0, 10)
            )
        }
    }

    fun guessHigher() {
        _petState.update { 
            if (it.gameState == GameState.Playing && it.gameType == "HigherLower") {
                it.copy(
                    gameState = GameState.Result,
                    gameAnimationCounter = 8,
                    gameAnswer = "higher"
                )
            } else it
        }
    }

    fun guessLower() {
        _petState.update { 
            if (it.gameState == GameState.Playing && it.gameType == "HigherLower") {
                it.copy(
                    gameState = GameState.Result,
                    gameAnimationCounter = 8,
                    gameAnswer = "lower"
                )
            } else it
        }
    }

    fun moveChimneyArrow() {
        _petState.update { 
            if (it.gameState == GameState.Playing && it.gameType == "GiftFinding") {
                it.copy(givenNumber = (it.givenNumber + 1) % 3)
            } else it
        }
    }

    fun selectChimney() {
        _petState.update { 
            if (it.gameState == GameState.Playing && it.gameType == "GiftFinding") {
                val rand = kotlin.random.Random.nextDouble()
                val result = when {
                    rand > 0.5 -> if (rand > 0.75) "object" else "pet"
                    else -> "soot"
                }
                it.copy(
                    gameState = GameState.Result,
                    gameAnimationCounter = 4,
                    chimneyResult = result
                )
            } else it
        }
    }

    fun evolveToSanta() {
        _petState.update { 
            it.copy(character = "santatchi", state = "idle", hungryPeriod = 3600, happyPeriod = 3600, lastEvolutionTimeSeconds = it.totalTimeSeconds)
        }
    }

    fun evolveToCabin() {
        _petState.update { 
            it.copy(character = "cabin", state = "idle", lastEvolutionTimeSeconds = it.totalTimeSeconds)
        }
    }

    fun setCharacter(name: String) {
        val info = PetConstants.evolutionData[name]
        _petState.update { 
            val next = if (info != null) {
                it.copy(
                    character = name,
                    hungryPeriod = info.hungryPeriod,
                    happyPeriod = info.happyPeriod,
                    poopPeriod = info.poopPeriod,
                    disciplineCallPeriod = info.disciplineCallPeriod,
                    sleepingHour = info.sleepingHour,
                    wakingHour = info.wakingHour,
                    yOffset = info.yOffset,
                    idealWeight = info.idealWeight,
                    walks = info.walks,
                    lastEvolutionTimeSeconds = it.totalTimeSeconds
                )
            } else if (name == "babytchi") {
                it.copy(
                    character = "babytchi",
                    hungryPeriod = 180,
                    happyPeriod = 240,
                    poopPeriod = 0,
                    disciplineCallPeriod = 5.5 * 3600.0,
                    sleepingHour = 20,
                    wakingHour = 9,
                    idealWeight = 5,
                    walks = true,
                    lastEvolutionTimeSeconds = it.totalTimeSeconds
                )
            } else {
                it.copy(character = name, lastEvolutionTimeSeconds = it.totalTimeSeconds)
            }
            with(PetEngine) { next.computeSleepWakeTimes() }
        }
        saveToDb()
    }

    fun setHunger(value: Int) {
        _petState.update { it.copy(stomach = value, timeSinceHungryChanged = 0.0, timeSinceHungryEmpty = 0.0) }
        saveToDb()
    }

    fun setHappiness(value: Int) {
        _petState.update { it.copy(happy = value, timeSinceHappyChanged = 0.0, timeSinceHappyEmpty = 0.0) }
        saveToDb()
    }

    fun triggerEvolution() {
        _petState.update { 
            val evolved = DarwinEngine.evolve(it)
            with(PetEngine) { evolved.computeSleepWakeTimes() }
        }
        saveToDb()
    }

    fun discipline() {
        _petState.update { 
            val base = if (it.needsDiscipline) {
                it.copy(discipline = (it.discipline + 25).coerceAtMost(100), needsDiscipline = false, timeSinceNeedsDiscipline = 0.0, state = "happy")
            } else {
                it.copy(state = "no")
            }
            base.copy(isScolding = true)
        }
        scope.launch {
            kotlinx.coroutines.delay(2000)
            _petState.update { 
                val next = if (it.state == "happy" || it.state == "no") it.copy(state = "idle") else it
                next.copy(isScolding = false)
            }
        }
        saveToDb()
    }

    fun toggleSuperTeen() {
        _petState.update { it.copy(isSuperTeen = !it.isSuperTeen) }
        saveToDb()
    }

    fun setPaused(paused: Boolean) {
        _petState.update { it.copy(isPaused = paused) }
        saveToDb()
    }

    fun setPetNameAndFinishNaming(name: String) {
        val isDebug = name.uppercase() == "DEBUG"
        _petState.update { 
            it.copy(
                petName = name,
                isDebugMode = it.isDebugMode || isDebug,
                character = "babytchi",
                state = "idle",
                gameState = GameState.None,
                isCalling = true
            ).let { s -> with(PetEngine) { s.computeSleepWakeTimes() } }
        }
        saveToDb()
    }

    fun setPetName(name: String) {
        _petState.update { it.copy(petName = name) }
        saveToDb()
    }
}
