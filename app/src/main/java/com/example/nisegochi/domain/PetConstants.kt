package com.example.nisegochi.domain

object PetConstants {
    data class EvolutionInfo(
        val hungryPeriod: Int,
        val happyPeriod: Int,
        val poopPeriod: Int,
        val disciplineCallPeriod: Double,
        val sleepingHour: Int,
        val wakingHour: Int,
        val yOffset: Int,
        val idealWeight: Int,
        val walks: Boolean
    )

    val evolutionData = mapOf(
        "tonmarutchi" to EvolutionInfo(45 * 60, 45 * 60, 3 * 3600, 5.5 * 3600, 20, 9, 0, 10, true),
        "tongaritchi" to EvolutionInfo(40 * 60, 50 * 60, 3 * 3600, 6.0 * 3600, 21, 9, 0, 20, false),
        "hashitamatchi" to EvolutionInfo(45 * 60, 45 * 60, 3 * 3600, 6.0 * 3600, 21, 9, 0, 20, true),
        "mimitchi" to EvolutionInfo(60 * 60, 60 * 60, 6 * 3600, 24.0 * 3600, 22, 8, 0, 30, false),
        "pochitchi" to EvolutionInfo(60 * 60, 55 * 60, 5 * 3600, 18.0 * 3600, 22, 8, 0, 30, true),
        "zuccitchi" to EvolutionInfo(50 * 60, 50 * 60, 5 * 3600, 6.0 * 3600, 23, 11, 0, 30, true),
        "hashizotchi" to EvolutionInfo(50 * 60, 50 * 60, 3 * 3600, 6.0 * 3600, 22, 10, 0, 30, true),
        "takotchi" to EvolutionInfo(55 * 60, 55 * 60, 5 * 3600, 1.0 * 3600, 23, 8, 0, 20, false),
        "kusatchi" to EvolutionInfo(40 * 60, 40 * 60, (1.5 * 3600).toInt(), 5.5 * 3600, 20, 10, 0, 20, true),
        "zatchi" to EvolutionInfo(55 * 60, 55 * 60, 5 * 3600, 1.0 * 3600, 23, 8, 0, 20, false),
        "santatchi" to EvolutionInfo(60 * 60, 60 * 60, 6 * 3600, 24.0 * 3600, 22, 9, 0, 30, true),
        "cabin" to EvolutionInfo(0, 0, 0, 0.0, 20, 9, 0, 10, false)
    )

    const val DIFFICULTY = 5

    val PANTRY_ITEMS = listOf(
        "meal_pie_1", "santa_cake_1", "bell_icon", "game_icon", "full_heart"
    )
}
