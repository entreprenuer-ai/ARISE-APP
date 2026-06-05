package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Graphs {
    @Serializable
    object Root : Graphs()

    @Serializable
    object Feature : Graphs()
}
