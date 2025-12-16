package dev.belalkhan.snapexplain.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.Explanation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExplanationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    
    private val explanationsCollection = firestore.collection("explanations")
    
    suspend fun saveExplanation(
        imageUri: Uri,
        codeSnippet: String,
        explanation: String,
        language: String
    ): Resource<Explanation> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("No user logged in")
        
        // Upload image to Firebase Storage
        val imageUrl = uploadImage(imageUri, userId)
        
        // Create explanation document
        val explanationModel = Explanation(
            userId = userId,
            imageUrl = imageUrl,
            codeSnippet = codeSnippet,
            explanation = explanation,
            language = language
        )
        
        val docRef = explanationsCollection.add(explanationModel).await()
        Resource.Success(explanationModel.copy(id = docRef.id))
    } catch (e: Exception) {
        Resource.Error("Failed to save explanation: ${e.message}", e)
    }
    
    private suspend fun uploadImage(imageUri: Uri, userId: String): String {
        val filename = "${userId}/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("code_images/$filename")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
    
    fun getAllExplanations(): Flow<Resource<List<Explanation>>> = callbackFlow {
        trySend(Resource.Loading)
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Resource.Error("No user logged in"))
            close()
            return@callbackFlow
        }
        
        val listener = explanationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error("Failed to fetch explanations: ${error.message}", error))
                    return@addSnapshotListener
                }
                
                val explanations = snapshot?.toObjects(Explanation::class.java) ?: emptyList()
                trySend(Resource.Success(explanations))
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getFavorites(): Flow<Resource<List<Explanation>>> = callbackFlow {
        trySend(Resource.Loading)
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Resource.Error("No user logged in"))
            close()
            return@callbackFlow
        }
        
        val listener = explanationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isFavorite", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error("Failed to fetch favorites: ${error.message}", error))
                    return@addSnapshotListener
                }
                
                val favorites = snapshot?.toObjects(Explanation::class.java) ?: emptyList()
                trySend(Resource.Success(favorites))
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun toggleFavorite(explanationId: String, isFavorite: Boolean): Resource<Unit> = try {
        explanationsCollection.document(explanationId)
            .update("isFavorite", isFavorite)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to update favorite: ${e.message}", e)
    }
    
    suspend fun deleteExplanation(explanationId: String): Resource<Unit> = try {
        explanationsCollection.document(explanationId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to delete explanation: ${e.message}", e)
    }
}
