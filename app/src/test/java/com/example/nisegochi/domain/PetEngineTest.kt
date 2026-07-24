package com.example.nisegochi.domain

import org.junit.Assert.*
import org.junit.Test

class PetEngineTest {

    @Test
    fun `test hunger decreases over time`() {
        val initialState = PetState(
            character = "babytchi",
            state = "idle",
            stomach = 4,
            hungryPeriod = 10,
            timeSinceHungryChanged = 0.0
        )
        
        var state = initialState
        // Tick 10 times
        for (i in 1..10) {
            state = PetEngine.tick(state)
        }
        
        assertEquals(3, state.stomach)
        assertEquals(0.0, state.timeSinceHungryChanged, 0.0)
    }

    @Test
    fun `test happiness decreases over time`() {
        val initialState = PetState(
            character = "babytchi",
            state = "idle",
            happy = 4,
            happyPeriod = 10,
            timeSinceHappyChanged = 0.0
        )
        
        var state = initialState
        // Tick 10 times
        for (i in 1..10) {
            state = PetEngine.tick(state)
        }
        
        assertEquals(3, state.happy)
        assertEquals(0.0, state.timeSinceHappyChanged, 0.0)
    }

    @Test
    fun `test evolution from babytchi to tonmarutchi`() {
        val initialState = PetState(
            character = "babytchi",
            state = "idle",
            totalTimeSeconds = 65.0 * 60.0 - 1
        )
        
        var state = PetEngine.tick(initialState) // Starts evolving
        assertEquals("evolving", state.state)
        assertEquals(5, state.evolutionAnimationCounter)
        
        // Tick 5 more times to complete animation
        for (i in 1..5) {
            state = PetEngine.tick(state)
        }
        
        assertEquals("tonmarutchi", state.character)
        assertEquals("idle", state.state)
    }
}
