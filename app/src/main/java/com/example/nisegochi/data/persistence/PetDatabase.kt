package com.example.nisegochi.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PetEntity::class], version = 9, exportSchema = false)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao

    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null

        private fun safeAddColumn(db: SupportSQLiteDatabase, sql: String) {
            try {
                db.execSQL(sql)
            } catch (e: Exception) {
                // Ignore "duplicate column name" error
                if (e.message?.contains("duplicate column name", ignoreCase = true) == false) {
                    throw e
                }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN isSuperTeen INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 3 added many fields for timers and stats
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN totalTimeSeconds REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN birthTimeMillis INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceHungryChanged REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceHungryEmpty REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceHappyChanged REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceHappyEmpty REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceLastPooped REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceDirty REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceSick REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceNeedsDiscipline REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeSinceNeedsLightsOff REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeToGetSickFromAge REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeForDisciplineCall REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeToSleep REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN timeToWake REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN careMisses INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN disciplineMistakes INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN cakesEaten INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Account for new fields from Hardcore Parity
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN isMuted INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN metabolismFactor REAL NOT NULL DEFAULT 1.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN metabolismEndTime REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN callStartTime REAL")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN disciplineCallStartTime REAL")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN pantry TEXT NOT NULL DEFAULT ''")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN hungryPeriod INTEGER NOT NULL DEFAULT 180")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN happyPeriod INTEGER NOT NULL DEFAULT 240")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN poopPeriod INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN disciplineCallPeriod REAL NOT NULL DEFAULT 19800.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN sleepingHour INTEGER NOT NULL DEFAULT 20")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN wakingHour INTEGER NOT NULL DEFAULT 9")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN idealWeight INTEGER NOT NULL DEFAULT 5")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN yOffset INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN selectedPantryIndex INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN evolutionAnimationCounter INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN deathAnimationCounter INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN nextCharacter TEXT")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN walks INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 5 fixes redundant 'walks' and adds missing fields
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN walks INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN lifespanBonus INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN isDebugMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN isPaused INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN petName TEXT NOT NULL DEFAULT ''")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN malnutritionTimer REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN obesityTimer REAL NOT NULL DEFAULT 0.0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN disciplineDecayTimer REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Catch any fields that might have been missed in earlier migrations
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN lifespanBonus INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN isDebugMode INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "ALTER TABLE pet_state ADD COLUMN disciplineDecayTimer REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 8 migration was missing or handled by fallback, adding as no-op
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // To fix the mismatch where 'lightsOn' (plural) existed in legacy data 
                // and 'isLightOn' (singular) is expected with a default value of 1,
                // we recreate the table with the correct schema.
                
                db.execSQL("ALTER TABLE pet_state RENAME TO pet_state_old")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `pet_state` (`id` INTEGER NOT NULL, `character` TEXT NOT NULL, `name` TEXT NOT NULL, `state` TEXT NOT NULL, `stomach` INTEGER NOT NULL, `happy` INTEGER NOT NULL, `weight` INTEGER NOT NULL, `age` INTEGER NOT NULL, `discipline` INTEGER NOT NULL, `isAlive` INTEGER NOT NULL, `isSleeping` INTEGER NOT NULL, `isDirty` INTEGER NOT NULL, `isSick` INTEGER NOT NULL, `isCalling` INTEGER NOT NULL, `needsDiscipline` INTEGER NOT NULL, `isLightOn` INTEGER NOT NULL DEFAULT 1, `walks` INTEGER NOT NULL DEFAULT 0, `isSuperTeen` INTEGER NOT NULL DEFAULT 0, `isPaused` INTEGER NOT NULL DEFAULT 0, `petName` TEXT NOT NULL DEFAULT '', `totalTimeSeconds` REAL NOT NULL DEFAULT 0.0, `birthTimeMillis` INTEGER NOT NULL DEFAULT 0, `lifespanBonus` INTEGER NOT NULL DEFAULT 0, `isDebugMode` INTEGER NOT NULL DEFAULT 0, `timeSinceHungryChanged` REAL NOT NULL DEFAULT 0.0, `timeSinceHungryEmpty` REAL NOT NULL DEFAULT 0.0, `timeSinceHappyChanged` REAL NOT NULL DEFAULT 0.0, `timeSinceHappyEmpty` REAL NOT NULL DEFAULT 0.0, `timeSinceLastPooped` REAL NOT NULL DEFAULT 0.0, `timeSinceDirty` REAL NOT NULL DEFAULT 0.0, `timeSinceSick` REAL NOT NULL DEFAULT 0.0, `timeSinceNeedsDiscipline` REAL NOT NULL DEFAULT 0.0, `timeSinceNeedsLightsOff` REAL NOT NULL DEFAULT 0.0, `timeToGetSickFromAge` REAL NOT NULL DEFAULT 0.0, `timeForDisciplineCall` REAL NOT NULL DEFAULT 0.0, `timeToSleep` REAL NOT NULL DEFAULT 0.0, `timeToWake` REAL NOT NULL DEFAULT 0.0, `malnutritionTimer` REAL NOT NULL DEFAULT 0.0, `obesityTimer` REAL NOT NULL DEFAULT 0.0, `disciplineDecayTimer` REAL NOT NULL DEFAULT 0.0, `careMisses` INTEGER NOT NULL DEFAULT 0, `disciplineMistakes` INTEGER NOT NULL DEFAULT 0, `cakesEaten` INTEGER NOT NULL DEFAULT 0, `metabolismFactor` REAL NOT NULL DEFAULT 1.0, `metabolismEndTime` REAL NOT NULL DEFAULT 0.0, `callStartTime` REAL, `disciplineCallStartTime` REAL, `hungryPeriod` INTEGER NOT NULL DEFAULT 180, `happyPeriod` INTEGER NOT NULL DEFAULT 240, `poopPeriod` INTEGER NOT NULL DEFAULT 0, `disciplineCallPeriod` REAL NOT NULL DEFAULT 19800.0, `sleepingHour` INTEGER NOT NULL DEFAULT 20, `wakingHour` INTEGER NOT NULL DEFAULT 9, `idealWeight` INTEGER NOT NULL DEFAULT 5, `yOffset` INTEGER NOT NULL DEFAULT 0, `isMuted` INTEGER NOT NULL DEFAULT 0, `pantry` TEXT NOT NULL DEFAULT '', `selectedPantryIndex` INTEGER NOT NULL DEFAULT 0, `evolutionAnimationCounter` INTEGER NOT NULL DEFAULT 0, `deathAnimationCounter` INTEGER NOT NULL DEFAULT 0, `nextCharacter` TEXT, `lastUpdatedTimestamp` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
                
                // Copy data, mapping old 'lightsOn' to new 'isLightOn' if it exists, otherwise use default
                // We check if lightsOn exists in the old table to avoid crash
                val cursor = db.query("PRAGMA table_info(pet_state_old)")
                var hasOldLightsOn = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "lightsOn") {
                        hasOldLightsOn = true
                        break
                    }
                }
                cursor.close()

                val lightOnSource = if (hasOldLightsOn) "lightsOn" else "1"
                
                db.execSQL("INSERT INTO pet_state (id, character, name, state, stomach, happy, weight, age, discipline, isAlive, isSleeping, isDirty, isSick, isCalling, needsDiscipline, isLightOn, walks, isSuperTeen, isPaused, petName, totalTimeSeconds, birthTimeMillis, lifespanBonus, isDebugMode, timeSinceHungryChanged, timeSinceHungryEmpty, timeSinceHappyChanged, timeSinceHappyEmpty, timeSinceLastPooped, timeSinceDirty, timeSinceSick, timeSinceNeedsDiscipline, timeSinceNeedsLightsOff, timeToGetSickFromAge, timeForDisciplineCall, timeToSleep, timeToWake, malnutritionTimer, obesityTimer, disciplineDecayTimer, careMisses, disciplineMistakes, cakesEaten, metabolismFactor, metabolismEndTime, callStartTime, disciplineCallStartTime, hungryPeriod, happyPeriod, poopPeriod, disciplineCallPeriod, sleepingHour, wakingHour, idealWeight, yOffset, isMuted, pantry, selectedPantryIndex, evolutionAnimationCounter, deathAnimationCounter, nextCharacter, lastUpdatedTimestamp) " +
                           "SELECT id, character, name, state, stomach, happy, weight, age, discipline, isAlive, isSleeping, isDirty, isSick, isCalling, needsDiscipline, $lightOnSource, walks, isSuperTeen, isPaused, petName, totalTimeSeconds, birthTimeMillis, lifespanBonus, isDebugMode, timeSinceHungryChanged, timeSinceHungryEmpty, timeSinceHappyChanged, timeSinceHappyEmpty, timeSinceLastPooped, timeSinceDirty, timeSinceSick, timeSinceNeedsDiscipline, timeSinceNeedsLightsOff, timeToGetSickFromAge, timeForDisciplineCall, timeToSleep, timeToWake, malnutritionTimer, obesityTimer, disciplineDecayTimer, careMisses, disciplineMistakes, cakesEaten, metabolismFactor, metabolismEndTime, callStartTime, disciplineCallStartTime, hungryPeriod, happyPeriod, poopPeriod, disciplineCallPeriod, sleepingHour, wakingHour, idealWeight, yOffset, isMuted, pantry, selectedPantryIndex, evolutionAnimationCounter, deathAnimationCounter, nextCharacter, lastUpdatedTimestamp FROM pet_state_old")
                
                db.execSQL("DROP TABLE pet_state_old")
            }
        }

        fun getDatabase(context: Context): PetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetDatabase::class.java,
                    "pet_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration() // Safety fallback
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

