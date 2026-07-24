package com.example.nisegochi.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class PetRoute : NavKey {
    @Serializable
    data object Main : PetRoute()
    
    @Serializable
    data object Clock : PetRoute()
    
    @Serializable
    data object Stats : PetRoute()
    
    @Serializable
    data object Food : PetRoute()
    
    @Serializable
    data object Game : PetRoute()
    
    @Serializable
    data object Pantry : PetRoute()
    
    @Serializable
    data object Naming : PetRoute()
    
    @Serializable
    data object Medicine : PetRoute()
    
    @Serializable
    data object Lights : PetRoute()
    
    @Serializable
    data object Toilet : PetRoute()
    
    @Serializable
    data object Discipline : PetRoute()
}
