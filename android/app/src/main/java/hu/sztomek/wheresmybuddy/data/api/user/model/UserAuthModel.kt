package hu.sztomek.wheresmybuddy.data.api.user.model

data class UserAuthModel(
        val id: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?
)