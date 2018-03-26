package hu.sztomek.wheresmybuddy.data.api.http

import hu.sztomek.wheresmybuddy.data.api.http.request.*
import hu.sztomek.wheresmybuddy.data.api.http.response.HandleConnectionRequestResponse
import hu.sztomek.wheresmybuddy.data.api.http.response.SendRequestResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface IRemoteApi {

    @POST("createConnectionRequest")
    fun sendRequest(@Body request: ConnectionRequest): Single<SendRequestResponse>
    @POST("cancelConnectionRequest")
    fun cancelRequest(@Query("requestId") requestId: String): Completable
    @POST("acceptConnectionRequest")
    fun acceptRequest(@Query("requestId") requestId: String): Single<HandleConnectionRequestResponse>
    @POST("declineConnectionRequest")
    fun declineRequest(@Query("requestId") requestId: String): Completable
    @POST("updateProfile")
    fun updateProfile(@Body request: UpdateProfileRequest, @Query("profileId") profileId: String): Completable
    @POST("requestLocation")
    fun requestLocation(@Body request: RequestLocationRequest): Completable
    @POST("addToken")
    fun updateGcmToken(@Body request: UpdateGcmTokenRequest): Completable
    @POST("uploadLocation")
    fun sendLocation(@Body request: UploadLocationRequest): Completable
    @POST("disconnect")
    fun unFriend(@Body request: UnFriendRequest): Completable
    @POST("updateConnection")
    fun updateConnection(@Body request: UpdateConnectionRequest): Completable

}