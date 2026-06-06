package com.pnm.habitsync.data.model

// This represents exactly what we will save in the Firebase Realtime Database
data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val photoUrl: String = "" // Optional for later
)