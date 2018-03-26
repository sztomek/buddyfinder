package hu.sztomek.wheresmybuddy.data.api.http.request

data class UploadLocationRequest(
        val to: String?,
        val public: Boolean?,
        val lat: Double?,
        val lng: Double?,
        val timestamp: Long?
)