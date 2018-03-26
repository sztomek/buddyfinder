package hu.sztomek.wheresmybuddy.presentation.model

data class LocationModel(
        val id: String?,
        val latitude: Double?,
        val longitude: Double?,
        val timestamp: Long?,
        val isPublic: Boolean,
        val receiverId: String?
)