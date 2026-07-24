package com.example.nisegochi.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nisegochi.data.persistence.PetDatabase
import com.example.nisegochi.data.persistence.toDomain
import com.example.nisegochi.data.persistence.toEntity
import com.example.nisegochi.domain.PetEngine
import com.example.nisegochi.domain.PetState
import com.example.nisegochi.ui.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val petDao = PetDatabase.getDatabase(context).petDao()
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val entity = petDao.getPetState() ?: return@withContext Result.success()
        val loadedState = entity.toDomain()
        val lastSaved = entity.lastUpdatedTimestamp
        val now = System.currentTimeMillis()
        
        val secondsPassed = (now - lastSaved) / 1000
        if (secondsPassed < 1) return@withContext Result.success()

        var currentState = loadedState
        
        // Limit catch up to 24 hours
        val maxCatchUpSeconds = 24 * 3600L
        val actualSecondsToCatchUp = secondsPassed.coerceAtMost(maxCatchUpSeconds)
        
        var notifyHungry = false
        var notifyHappy = false
        var notifySick = false
        var notifyEvolved = false
        var notifyCall = false
        var lastChar = currentState.character

        repeat(actualSecondsToCatchUp.toInt()) {
            val previousState = currentState
            currentState = PetEngine.tick(currentState)
            
            if (!currentState.isAlive) return@repeat

            if (currentState.stomach == 0 && previousState.stomach > 0) notifyHungry = true
            if (currentState.happy == 0 && previousState.happy > 0) notifyHappy = true
            if (currentState.isSick && !previousState.isSick) notifySick = true
            if (currentState.isCalling && !previousState.isCalling) notifyCall = true
            if (currentState.character != lastChar) {
                notifyEvolved = true
                lastChar = currentState.character
            }
        }

        // Save new state
        petDao.insertPetState(currentState.toEntity(now))

        // Trigger notifications
        if (currentState.isAlive) {
            when {
                notifyEvolved -> notificationHelper.showNotification("Evolution!", "Your pet has evolved! Check it out!")
                notifySick -> notificationHelper.showNotification("SICK!", "Your pet is sick! It needs medicine!")
                notifyCall -> notificationHelper.showNotification("ATTENTION!", "Your pet needs you!")
                notifyHungry && notifyHappy -> notificationHelper.showNotification("ATTENTION!", "Your pet is hungry and sad!")
                notifyHungry -> notificationHelper.showNotification("HUNGRY!", "Your pet is hungry! Feed it!")
                notifyHappy -> notificationHelper.showNotification("SAD!", "Your pet is sad! Play with it!")
            }
        } else if (loadedState.isAlive) {
             notificationHelper.showNotification("Oh no!", "Your pet has passed away... :( ")
        }

        Result.success()
    }
}
