package com.example.nisegochi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.domain.GameState
import com.example.nisegochi.ui.navigation.PetRoute
import com.example.nisegochi.ui.screens.ClockScreen
import com.example.nisegochi.ui.screens.FoodScreen
import com.example.nisegochi.ui.screens.GameScreen
import com.example.nisegochi.ui.screens.MainScreen
import com.example.nisegochi.ui.screens.NamingScreen
import com.example.nisegochi.ui.screens.PantryScreen
import com.example.nisegochi.ui.screens.StatsScreen
import com.example.nisegochi.ui.viewmodel.PetViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptivePetLayout(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(elements = arrayOf(PetRoute.Main))
    
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collectLatest { route ->
            if (backStack.last() != route) {
                backStack.add(route)
            }
        }
    }
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val isExpanded = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    val highlightedIcon by viewModel.highlightedIcon.collectAsState()
    val debugMode by viewModel.debugMode.collectAsState()
    val petState by viewModel.petState.collectAsState()

    val onAAction: () -> Unit = {
        when (backStack.last()) {
            PetRoute.Main -> viewModel.cycleIcon()
            PetRoute.Clock -> viewModel.cycleClockSelection()
            PetRoute.Food -> viewModel.cycleFoodSelection()
            PetRoute.Pantry -> viewModel.cyclePantrySelection()
            PetRoute.Game -> {
                if (petState.gameType == "GiftFinding") viewModel.moveChimneyArrow()
                else viewModel.guessHigher()
            }
            PetRoute.Naming -> viewModel.namingNextLetter()
            else -> {}
        }
    }

    val onBAction: () -> Unit = {
        when (backStack.last()) {
            PetRoute.Main -> handleSelection(highlightedIcon, backStack, viewModel)
            PetRoute.Clock -> viewModel.incrementClockValue()
            PetRoute.Food -> {
                viewModel.confirmFood()
                backStack.removeAt(backStack.size - 1)
            }
            PetRoute.Pantry -> {
                viewModel.usePantryItem()
                backStack.removeAt(backStack.size - 1)
            }
            PetRoute.Game -> {
                if (petState.gameType == "GiftFinding") viewModel.selectChimney()
                else viewModel.guessLower()
            }
            PetRoute.Naming -> viewModel.namingSelectLetter()
            else -> {}
        }
    }

    val onCAction: () -> Unit = {
        when (backStack.last()) {
            PetRoute.Main -> {
                if (highlightedIcon == 1 && petState.character.startsWith("santa")) {
                    viewModel.togglePantry()
                } else {
                    backStack.add(PetRoute.Clock)
                }
            }
            PetRoute.Clock -> {
                if (!viewModel.onClockCPress()) {
                    backStack.removeAt(backStack.size - 1)
                }
            }
            PetRoute.Naming -> {
                viewModel.namingDone()
                backStack.removeAt(backStack.size - 1)
            }
            else -> {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isExpanded) {
            PetDeviceShell(
                viewModel = viewModel,
                backStack = backStack,
                onA = onAAction,
                onB = onBAction,
                onC = onCAction,
                modifier = Modifier.fillMaxSize()
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    transitionSpec = {
                        val from = initialState.key
                        val to = targetState.key
                        if ((from == PetRoute.Main && to == PetRoute.Clock) ||
                            (from == PetRoute.Clock && to == PetRoute.Main)) {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                    popTransitionSpec = {
                        val from = initialState.key
                        val to = targetState.key
                        if ((from == PetRoute.Main && to == PetRoute.Clock) ||
                            (from == PetRoute.Clock && to == PetRoute.Main)) {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    }
                ) { key ->
                    NavEntry(key) {
                        when (key) {
                            PetRoute.Main -> MainScreen(viewModel)
                            PetRoute.Clock -> ClockScreen(viewModel)
                            PetRoute.Stats -> StatsScreen(viewModel)
                            PetRoute.Game -> GameScreen(viewModel)
                            PetRoute.Food -> FoodScreen(viewModel)
                            PetRoute.Pantry -> PantryScreen(viewModel)
                            PetRoute.Naming -> NamingScreen(viewModel)
                            else -> MainScreen(viewModel)
                        }
                    }
                }
            }
        } else {
            // Tablet Layout: Adaptive Scaffolding
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                sceneStrategy = listDetailStrategy
            ) { key ->
                when (key) {
                    PetRoute.Main -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        PetDeviceShell(
                            viewModel = viewModel,
                            backStack = backStack,
                            onA = onAAction,
                            onB = onBAction,
                            onC = onCAction,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            MainScreen(viewModel)
                        }
                    }
                    PetRoute.Stats -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        StatsScreen(viewModel)
                    }
                    PetRoute.Clock -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        ClockScreen(viewModel)
                    }
                    PetRoute.Game -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        GameScreen(viewModel)
                    }
                    PetRoute.Food -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        FoodScreen(viewModel)
                    }
                    PetRoute.Pantry -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        PantryScreen(viewModel)
                    }
                    PetRoute.Naming -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        NamingScreen(viewModel)
                    }
                    else -> NavEntry(key) { MainScreen(viewModel) }
                }
            }
        }
        
        if (debugMode) {
            DebugOverlay(petState, viewModel)
        }
    }
}

@Composable
fun DebugOverlay(state: PetState, viewModel: PetViewModel) {
    Surface(
        color = Color.Black.copy(alpha = 0.7f),
        contentColor = Color.Green,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text("DEBUG MODE", fontSize = 12.sp)
            Text("Char: ${state.character} Age: ${state.age}", fontSize = 10.sp)
            Text("Hungry: ${state.stomach} Happy: ${state.happy}", fontSize = 10.sp)
            Text("Weight: ${state.weight} Discipline: ${state.discipline}", fontSize = 10.sp)
            Text("Misses: ${state.careMisses} DMs: ${state.disciplineMistakes}", fontSize = 10.sp)
            Text("Time: ${state.totalTimeSeconds.toInt()}s ST: ${state.isSuperTeen}", fontSize = 10.sp)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Character Switching
            val characters = listOf(
                "babytchi", "tonmarutchi", "tongaritchi", "hashitamatchi",
                "mimitchi", "pochitchi", "zuccitchi", "hashizotchi",
                "takotchi", "kusatchi", "zatchi", "santatchi", "cabin"
            )
            
            Text("CHARACTERS:", fontSize = 8.sp)
            val chunkedChars = characters.chunked(4)
            chunkedChars.forEach { rowChars ->
                Row {
                    rowChars.forEach { char ->
                        androidx.compose.material3.Button(
                            onClick = { viewModel.setCharacter(char) },
                            contentPadding = PaddingValues(2.dp),
                            modifier = Modifier.height(20.dp).padding(end = 2.dp)
                        ) {
                            Text(char.take(4).uppercase(), fontSize = 6.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("STATS:", fontSize = 8.sp)
            Row {
                androidx.compose.material3.Button(
                    onClick = { viewModel.setHunger(0) },
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("HUNGER 0", fontSize = 8.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.material3.Button(
                    onClick = { viewModel.setHappiness(0) },
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("HAPPY 0", fontSize = 8.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.material3.Button(
                    onClick = { viewModel.toggleSuperTeen() },
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("ST", fontSize = 8.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.material3.Button(
                onClick = { viewModel.triggerEvolution() },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.height(24.dp).fillMaxWidth()
            ) {
                Text("TRIGGER EVOLUTION", fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun PetDeviceShell(
    viewModel: PetViewModel,
    backStack: NavBackStack<NavKey>,
    onA: () -> Unit,
    onB: () -> Unit,
    onC: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pressedButtons = remember { mutableStateMapOf<String, Boolean>() }
    val handledByCombination = remember { mutableStateMapOf<String, Boolean>() }

    fun checkCombinations() {
        val a = pressedButtons["A"] == true
        val b = pressedButtons["B"] == true
        val c = pressedButtons["C"] == true

        if (a && b && c) {
            viewModel.reset()
            handledByCombination["A"] = true
            handledByCombination["B"] = true
            handledByCombination["C"] = true
        } else if (a && c) {
            viewModel.toggleMute()
            handledByCombination["A"] = true
            handledByCombination["C"] = true
        } else if (a && b) {
            viewModel.togglePause()
            handledByCombination["A"] = true
            handledByCombination["B"] = true
        } else if (b && c) {
            viewModel.enterClockSetMode()
            handledByCombination["B"] = true
            handledByCombination["C"] = true
            if (backStack.last() != PetRoute.Clock) {
                backStack.add(PetRoute.Clock)
            }
        }
    }

    val internalOnA = {
        if (handledByCombination["A"] == true) {
            handledByCombination["A"] = false
        } else {
            onA()
        }
    }

    val internalOnB = {
        if (handledByCombination["B"] == true) {
            handledByCombination["B"] = false
        } else {
            onB()
        }
    }

    val internalOnC = {
        if (handledByCombination["C"] == true) {
            handledByCombination["C"] = false
        } else {
            onC()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The Screen
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFFC0C0C0), // Outer shell color
            shape = MaterialTheme.shapes.large
        ) {
            content()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // The Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PetButton(
                label = "A",
                onClick = internalOnA,
                onPressStateChanged = { pressed ->
                    pressedButtons["A"] = pressed
                    if (pressed) checkCombinations()
                }
            )
            PetButton(
                label = "B",
                onClick = internalOnB,
                onPressStateChanged = { pressed ->
                    pressedButtons["B"] = pressed
                    if (pressed) checkCombinations()
                }
            )
            PetButton(
                label = "C",
                onClick = internalOnC,
                onPressStateChanged = { pressed ->
                    pressedButtons["C"] = pressed
                    if (pressed) checkCombinations()
                }
            )
        }
    }
}

private fun handleSelection(
    iconIndex: Int,
    backStack: NavBackStack<NavKey>,
    viewModel: PetViewModel
) {
    val state = viewModel.petState.value
    val isPantryMode = viewModel.pantryMode.value

    when (iconIndex) {
        1 -> {
            if (isPantryMode && state.character.startsWith("santa")) {
                backStack.add(PetRoute.Pantry)
            } else {
                backStack.add(PetRoute.Food)
            }
        }
        2 -> viewModel.toggleLights()
        3 -> {
            viewModel.startGame()
            backStack.add(PetRoute.Game)
        }
        4 -> viewModel.heal() // Medicine
        5 -> viewModel.clean() // Toilet
        6 -> backStack.add(PetRoute.Stats)
        7 -> viewModel.discipline() // Discipline
    }
}
