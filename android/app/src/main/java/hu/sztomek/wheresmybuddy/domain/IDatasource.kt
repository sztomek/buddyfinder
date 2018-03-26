package hu.sztomek.wheresmybuddy.domain

import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface IDatasource {

    // firebase auth methods
    fun hasActiveUser(): Single<Boolean>
    fun getActiveUser(): Single<UserModel>
    fun logout(): Completable
    fun deleteAccount(): Completable

    // firestore methods
    fun getProfile(userId: String): Single<UserModel>
    fun getLastLocation(userId: String, selfUserId: String): Maybe<LocationModel>
    fun getLocation(locationId: String): Single<LocationModel>
    fun searchFrom(query: String, lastId: String?, limit: Int): Single<List<UserModel>>
    fun searchAllUntil(query: String, lastId: String): Single<List<UserModel>>
    fun getConnectionRequestBetween(user1: String, user2: String): Maybe<ConnectionRequestModel>
    fun getConnectionBetween(user1: String, user2: String): Maybe<ConnectionModel>
    fun listConnections(userId: String, nameFilter: String?, lastId: String?, trustedOnly: Boolean, limit: Int): Single<List<ConnectionModel>>
    fun listAllConnectionsUntil(userId: String, nameFilter: String?, lastId: String, trustedOnly: Boolean): Single<List<ConnectionModel>>
    fun listIncomingRequests(userId: String, filterName: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestModel>>
    fun listAllIncomingRequestsUntil(userId: String, filterName: String?, lastId: String): Single<List<ConnectionRequestModel>>
    fun listOutgoingRequests(userId: String, filterName: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestModel>>
    fun listAllOutgoingRequestsUntil(userId: String, filterName: String?, lastId: String): Single<List<ConnectionRequestModel>>

    // functions methods
    fun updateProfile(profile: UserModel): Completable
    fun updateGcmToken(gcmToken: String): Completable
    fun sendLocationRequest(to: String): Completable
    fun uploadLocation(location: LocationModel): Completable
    fun updateConnection(newLevel: Int, userId: String): Completable
    fun deleteConnection(userId: String): Completable
    fun sendConnectionRequest(friendId: String): Single<ConnectionRequestModel>
    fun cancelConnectionRequest(requestId: String): Completable
    fun acceptConnectionRequest(requestId: String): Single<ConnectionModel>
    fun declineConnectionRequest(requestId: String): Completable

}