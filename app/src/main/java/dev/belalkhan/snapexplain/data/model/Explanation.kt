package dev.belalkhan.snapexplain.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Explanation(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val codeSnippet: String = "",
    val explanation: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    @PropertyName("favorite")
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val language: String = ""
)
