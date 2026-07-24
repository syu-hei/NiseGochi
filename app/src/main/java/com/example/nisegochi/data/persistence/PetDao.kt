package com.example.nisegochi.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pet_state WHERE id = 1")
    suspend fun getPetState(): PetEntity?

    @Query("SELECT * FROM pet_state WHERE id = 1")
    fun observePetState(): Flow<PetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetState(petState: PetEntity)
}
