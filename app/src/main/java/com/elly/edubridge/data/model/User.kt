package com.elly.edubridge.data.model

data class User(
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val country: String = "",
    val city: String = "",
    val institution: String = "",
    val course: String = "",
    val yearOfStudy: String = "",
    val bio: String = "",
    val headline: String = "",
    val profileImage: String = "",

    val skillsOffered: List<String> = emptyList(),
    val skillsWanted: List<String> = emptyList(),

    val portfolioLinks: List<String> = emptyList(),

    val rating: Double = 0.0,
    val completedTrades: Int = 0,

    val isVerified: Boolean = false,

    // NEW FIELDS

    val availability: String = "Available",

    val preferredLearningMode: String = "Online",

    val interests: List<String> = emptyList(),

    val socialLinks: Map<String, String> = emptyMap(),

    val featuredSkill: String = "",

    val profileCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    val lastActive: Long = System.currentTimeMillis()
)