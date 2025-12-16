package dev.belalkhan.snapexplain.data.repository

import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val oneTapClient: SignInClient,
    private val signInRequest: BeginSignInRequest
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    // Google Sign-In
    suspend fun beginSignIn(): Resource<IntentSender> = try {
        val result = oneTapClient.beginSignIn(signInRequest).await()
        Resource.Success(result.pendingIntent.intentSender)
    } catch (e: Exception) {
        Resource.Error("Failed to start sign in: ${e.message}", e)
    }
    
    suspend fun signInWithGoogle(intent: Intent): Resource<FirebaseUser> = try {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        
        val authResult = auth.signInWithCredential(googleCredentials).await()
        val user = authResult.user ?: throw Exception("User is null")
        
        // Create or update user profile
        createOrUpdateUserProfile(user)
        
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error("Sign in failed: ${e.message}", e)
    }
    
    // Email/Password Sign-In
    suspend fun signInWithEmail(email: String, password: String): Resource<FirebaseUser> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Resource.Error("Email and password cannot be empty")
            }

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User is null")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("Sign in failed: ${e.message}", e)
        }
    }
    
    // Email/Password Sign-Up
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Resource<FirebaseUser> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Resource.Error("Email and password cannot be empty")
            }

            if (password.length < 6) {
                return Resource.Error("Password must be at least 6 characters")
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User is null")

            // Create user profile with display name
            val profile = UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                displayName = displayName.ifBlank { email.substringBefore("@") },
                photoUrl = ""
            )
            firestore.collection("users").document(user.uid).set(profile).await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("Sign up failed: ${e.message}", e)
        }
    }
    
    private suspend fun createOrUpdateUserProfile(user: FirebaseUser) {
        val userRef = firestore.collection("users").document(user.uid)
        val doc = userRef.get().await()
        
        if (!doc.exists()) {
            val profile = UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "",
                photoUrl = user.photoUrl?.toString() ?: ""
            )
            userRef.set(profile).await()
        }
    }
    
    fun getUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)
        try {
            val userId = currentUser?.uid ?: throw Exception("No user logged in")
            val doc = firestore.collection("users").document(userId).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: throw Exception("Profile not found")
            emit(Resource.Success(profile))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch profile: ${e.message}", e))
        }
    }
    
    suspend fun updateUserStats(
        totalExplanations: Int? = null,
        favoriteCount: Int? = null,
        dailyLearningTime: Long? = null,
        studentScore: Int? = null
    ): Resource<Unit> = try {
        val userId = currentUser?.uid ?: throw Exception("No user logged in")
        val updates = mutableMapOf<String, Any>()
        
        totalExplanations?.let { updates["totalExplanations"] = it }
        favoriteCount?.let { updates["favoriteCount"] = it }
        dailyLearningTime?.let { updates["dailyLearningTime"] = it }
        studentScore?.let { updates["studentScore"] = it }
        
        firestore.collection("users").document(userId).update(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to update stats: ${e.message}", e)
    }
    
    fun signOut() {
        auth.signOut()
        oneTapClient.signOut()
    }
}

