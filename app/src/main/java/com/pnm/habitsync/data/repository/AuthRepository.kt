package com.pnm.habitsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pnm.habitsync.data.model.User
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.tasks.await

class AuthRepository(
    // We pass instances in the constructor. This makes testing easier later!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    // Quickly check if a user is already logged in when they open the app
    val currentUser = auth.currentUser

    // 1. LOGIN FUNCTION
    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            // Talk to Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // Return success with basic user info
                Resource.Success(User(uid = user.uid, email = user.email ?: ""))
            } else {
                Resource.Error("Login failed: Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during login")
        }
    }

    // 2. REGISTER FUNCTION
    suspend fun register(email: String, password: String, username: String): Resource<User> {
        return try {
            // Step A: Create the user in Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val newUser = User(uid = firebaseUser.uid, email = email, username = username)

                // Step B: Save the extra details (like username) into Realtime Database
                // Path: users/userId/...
                db.child("users").child(firebaseUser.uid).setValue(newUser).await()

                Resource.Success(newUser)
            } else {
                Resource.Error("Registration failed: User is null")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during registration")
        }
    }
}