package dev.belalkhan.snapexplain.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val totalExplanations: Int = 0,
    val favoriteCount: Int = 0,
    val dailyLearningTime: Long = 0L, // in milliseconds
    val studentScore: Int = 0, // 0-100
    @ServerTimestamp
    val joinedDate: Timestamp? = null
)
