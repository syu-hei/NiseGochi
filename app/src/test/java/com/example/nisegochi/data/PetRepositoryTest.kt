package com.example.nisegochi.data

import com.example.nisegochi.data.persistence.PetDao
import com.example.nisegochi.data.persistence.PetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakePetDao : PetDao {
    private var state: PetEntity? = null
    override suspend fun getPetState(): PetEntity? = state
    override fun observePetState(): Flow<PetEntity?> = flowOf(state)
    override suspend fun insertPetState(petState: PetEntity) {
        state = petState
    }
}

class PetRepositoryTest {
    private lateinit var repository: PetRepository
    private lateinit var dao: FakePetDao

    @Before
    fun setup() {
        dao = FakePetDao()
        repository = PetRepository(dao)
    }

    @Test
    fun testSetHunger() = runBlocking {
        repository.setHunger(2)
        assertEquals(2, repository.petState.value.stomach)
    }

    @Test
    fun testSetHappiness() = runBlocking {
        repository.setHappiness(3)
        assertEquals(3, repository.petState.value.happy)
    }

    @Test
    fun testSetCharacter() = runBlocking {
        repository.setCharacter("mimitchi")
        assertEquals("mimitchi", repository.petState.value.character)
        // Verify stats are updated (mimitchi has 3600s hungryPeriod)
        assertEquals(3600, repository.petState.value.hungryPeriod)
    }

    @Test
    fun testTriggerEvolution() = runBlocking {
        repository.setCharacter("babytchi")
        repository.triggerEvolution()
        assertEquals("tonmarutchi", repository.petState.value.character)
    }
}
