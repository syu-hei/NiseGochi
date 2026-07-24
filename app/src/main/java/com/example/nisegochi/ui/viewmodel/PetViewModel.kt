package com.example.nisegochi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nisegochi.data.PetRepository
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.domain.GameState
import com.example.nisegochi.ui.audio.AudioManager
import com.example.nisegochi.ui.navigation.PetRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PetViewModel(private val repository: PetRepository) : ViewModel() {

    val petState: StateFlow<PetState> = repository.petState
    
    private val _highlightedIcon = MutableStateFlow(1) // Start with first icon highlighted
    val highlightedIcon = _highlightedIcon.asStateFlow()

    init {
        startEngine()
        observeStateForSounds()
        observeGameStateForNavigation()
    }

    private val _navigationEvent = MutableSharedFlow<PetRoute>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private fun observeGameStateForNavigation() {
        viewModelScope.launch {
            petState.map { it.gameState }.distinctUntilChanged().collect { gameState ->
                if (gameState == GameState.Naming) {
                    _navigationEvent.emit(PetRoute.Naming)
                }
            }
        }
    }

    private val _pantryMode = MutableStateFlow(false)
    val pantryMode = _pantryMode.asStateFlow()

    fun togglePantry() {
        if (petState.value.character.startsWith("santa")) {
            _pantryMode.value = !_pantryMode.value
        }
    }

    private fun observeStateForSounds() {
        viewModelScope.launch {
            var lastChar = petState.value.character
            var lastGameState = petState.value.gameState
            var lastIsCalling = petState.value.isCalling
            var lastIsAlive = petState.value.isAlive
            
            petState.collect { state ->
                AudioManager.isMuted = state.isMuted
                
                if (state.isAlive != lastIsAlive && !state.isAlive) {
                    AudioManager.playSound("death_march")
                }
                lastIsAlive = state.isAlive

                if (state.character != lastChar) {
                    if (lastChar == "egg" && state.character == "babytchi") {
                        AudioManager.playSound("hatching_sound")
                    } else if (lastChar != "egg") {
                        AudioManager.playSound("evolve_sound")
                        AudioManager.playSound("fanfare")
                    }
                    
                    if (lastChar.startsWith("santa") && !state.character.startsWith("santa")) {
                        AudioManager.playSound("santa_exit")
                    }
                    lastChar = state.character
                }
                
                if (state.gameState != lastGameState) {
                    val isSanta = state.character.startsWith("santa")
                    when (state.gameState) {
                        GameState.Intro -> AudioManager.playSound(if (isSanta) "santa_small_beep" else "game_begin")
                        GameState.Result -> AudioManager.playSound(if (isSanta) "santa_validate_sound" else "flip_sound")
                        GameState.FinalResult -> {
                            val win = if (state.gameType == "GiftFinding") state.chimneyResult != "soot" else state.gameScore >= 3
                            if (win) {
                                AudioManager.playSound("fanfare")
                            } else {
                                AudioManager.playSound(if (isSanta) "santa_small_beep" else "display_results_sound")
                            }
                        }
                        else -> {}
                    }
                    lastGameState = state.gameState
                }

                if (state.isCalling && !lastIsCalling) {
                    AudioManager.playSound("call")
                }
                lastIsCalling = state.isCalling
            }
        }
    }

    private fun startEngine() {
        viewModelScope.launch {
            repository.isLoaded.first { it }
            while (isActive) {
                repository.update()
                delay(1000) // 1 second tick
            }
        }
    }

    fun cycleIcon() {
        _highlightedIcon.update { if (it >= 8) 1 else it + 1 }
    }

    private val _debugMode = MutableStateFlow(false)
    val debugMode: StateFlow<Boolean> = combine(_debugMode, petState) { localDebug, state ->
        localDebug || state.isDebugMode
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    private val _isClockSetMode = MutableStateFlow(false)
    val isClockSetMode = _isClockSetMode.asStateFlow()
    
    private val _clockSelection = MutableStateFlow(0) // 0: Hour, 1: Minute
    val clockSelection = _clockSelection.asStateFlow()
    
    private val _foodSelection = MutableStateFlow(0) // 0: Meal, 1: Snack
    val foodSelection = _foodSelection.asStateFlow()
    
    fun cycleFoodSelection() {
        _foodSelection.update { (it + 1) % 2 }
    }

    private var debugClickCount = 0

    fun onClockCPress(): Boolean {
        if (_isClockSetMode.value) {
            _isClockSetMode.value = false
            return true
        }
        debugClickCount++
        if (debugClickCount >= 10) {
            _debugMode.value = !_debugMode.value
            debugClickCount = 0
        }
        return false
    }

    fun cycleClockSelection() {
        if (_isClockSetMode.value) {
            _clockSelection.value = (_clockSelection.value + 1) % 2
        }
    }

    fun incrementClockValue() {
        if (_isClockSetMode.value) {
            val secondsToAdd = if (_clockSelection.value == 0) 3600L else 60L
            adjustClock(secondsToAdd)
        }
    }

    fun enterClockSetMode() {
        _isClockSetMode.value = true
    }
    
    fun adjustClock(seconds: Long) {
        repository.adjustClock(seconds)
    }

    fun selectIcon() {
        // Handle selection based on highlightedIcon
        when (_highlightedIcon.value) {
            1 -> { /* Food - handled by UI navigation usually */ }
            2 -> toggleLights()
            3 -> startGame()
            5 -> clean()
            4 -> heal()
            7 -> discipline()
            // ...
        }
    }

    fun startGame() = repository.startGame()
    fun guessHigher() = repository.guessHigher()
    fun guessLower() = repository.guessLower()
    fun moveChimneyArrow() = repository.moveChimneyArrow()
    fun selectChimney() = repository.selectChimney()
    fun cyclePantrySelection() = repository.cyclePantrySelection()
    fun usePantryItem() = repository.usePantryItem()
    fun evolveToSanta() = repository.evolveToSanta()
    fun evolveToCabin() = repository.evolveToCabin()
    fun setCharacter(name: String) = repository.setCharacter(name)
    fun setHunger(value: Int) = repository.setHunger(value)
    fun setHappiness(value: Int) = repository.setHappiness(value)
    fun triggerEvolution() = repository.triggerEvolution()
    
    fun clearSelection() {
        _highlightedIcon.value = 0
    }

    fun feed(isMeal: Boolean) = repository.feed(isMeal)
    fun confirmFood() {
        feed(_foodSelection.value == 0)
    }
    fun play() = repository.play()
    fun clean() = repository.clean()
    fun heal() = repository.heal()
    fun discipline() = repository.discipline()
    fun toggleSuperTeen() = repository.toggleSuperTeen()
    fun toggleMute() = repository.toggleMute()
    fun toggleLights() = repository.toggleLights()
    fun togglePause() = repository.setPaused(!petState.value.isPaused)
    fun reset() = repository.reset()

    // Naming logic
    private val _namingName = MutableStateFlow("")
    val namingName = _namingName.asStateFlow()
    
    private val _namingIndex = MutableStateFlow(1) // 'A'
    val namingIndex = _namingIndex.asStateFlow()
    
    private val letters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun namingNextLetter() {
        _namingIndex.update { if (it >= letters.length - 1) 0 else it + 1 }
    }
    
    fun namingSelectLetter() {
        if (_namingName.value.length < 8) {
            val letter = letters.getOrNull(_namingIndex.value) ?: ' '
            _namingName.update { it + letter }
        }
    }
    
    fun namingDone() {
        repository.setPetNameAndFinishNaming(_namingName.value)
        _namingName.value = ""
        _namingIndex.value = 1
    }
}
