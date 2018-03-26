package hu.sztomek.wheresmybuddy.data.api.http.request

data class UpdateProfileRequest(
        val displayName: String,
        val photoUrl: String
)