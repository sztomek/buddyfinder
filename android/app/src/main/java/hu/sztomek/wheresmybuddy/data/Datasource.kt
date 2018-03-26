package hu.sztomek.wheresmybuddy.data

import hu.sztomek.wheresmybuddy.data.api.db.IDatabaseApi
import hu.sztomek.wheresmybuddy.data.api.db.model.toDomainModel
import hu.sztomek.wheresmybuddy.data.api.http.IRemoteApi
import hu.sztomek.wheresmybuddy.data.api.http.model.toDomainModel
import hu.sztomek.wheresmybuddy.data.api.http.request.*
import hu.sztomek.wheresmybuddy.data.api.user.IUserApi
import hu.sztomek.wheresmybuddy.data.api.user.model.toDomainModel
import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class Datasource @Inject constructor(
        private val userApi: IUserApi,
        private val remoteApi: IRemoteApi,
        private val databaseApi: IDatabaseApi
) : IDatasource {

    override fun hasActiveUser(): Single<Boolean> {
        return userApi.hasActiveUser()
    }

    override fun getActiveUser(): Single<UserModel> {
        return userApi.getUser()
                .map {
                    it.toDomainModel()
                }
    }

    override fun logout(): Completable {
        return userApi.logout()
    }

    override fun deleteAccount(): Completable {
        return userApi.deleteAccount()
    }

    override fun getProfile(userId: String): Single<UserModel> {
        return databaseApi.getProfile(userId)
                .map {
                    it.toDomainModel()
                }
    }

    override fun getLastLocation(userId: String, selfUserId: String): Maybe<LocationModel> {
        return databaseApi.getLastLocation(userId, selfUserId)
                .map {
                    it.toDomainModel()
                }
    }

    override fun getLocation(locationId: String): Single<LocationModel> {
        return databaseApi.getLocation(locationId)
                .map {
                    it.toDomainModel()
                }
    }

    override fun searchFrom(query: String, lastId: String?, limit: Int): Single<List<UserModel>> {
        return databaseApi.searchProfilesAfter(query, lastId, limit)
                .map {
                    val models = ArrayList<UserModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun searchAllUntil(query: String, lastId: String): Single<List<UserModel>> {
        return databaseApi.searchAllProfilesUntil(query, lastId)
                .map {
                    val models = ArrayList<UserModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun getConnectionRequestBetween(user1: String, user2: String): Maybe<ConnectionRequestModel> {
        return databaseApi.getPendingConnectionBetween(user1, user2)
                .map {
                    it.toDomainModel()
                }
    }

    override fun getConnectionBetween(user1: String, user2: String): Maybe<ConnectionModel> {
        return databaseApi.getConnectionBetween(user1, user2)
                .map {
                    it.toDomainModel(user1)
                }
    }

    override fun listConnections(userId: String, nameFilter: String?, lastId: String?, trustedOnly: Boolean, limit: Int): Single<List<ConnectionModel>> {
        return databaseApi.listConnections(userId, nameFilter, lastId, trustedOnly, limit)
                .map {
                    val models = ArrayList<ConnectionModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel(userId)
                        )
                    }
                    return@map models
                }
    }

    override fun listAllConnectionsUntil(userId: String, nameFilter: String?, lastId: String, trustedOnly: Boolean): Single<List<ConnectionModel>> {
        return databaseApi.listAllConnectionsUntil(userId, nameFilter, lastId, trustedOnly)
                .map {
                    val models = ArrayList<ConnectionModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel(userId)
                        )
                    }
                    return@map models
                }
    }

    override fun listIncomingRequests(userId: String, filterName: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestModel>> {
        return databaseApi.listIncomingRequests(userId, filterName, lastId, limit)
                .map {
                    val models = ArrayList<ConnectionRequestModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun listAllIncomingRequestsUntil(userId: String, filterName: String?, lastId: String): Single<List<ConnectionRequestModel>> {
        return databaseApi.listAllIncomingRequestsUntil(userId, filterName, lastId)
                .map {
                    val models = ArrayList<ConnectionRequestModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun listOutgoingRequests(userId: String, filterName: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestModel>> {
        return databaseApi.listOutgoingRequests(userId, filterName, lastId, limit)
                .map {
                    val models = ArrayList<ConnectionRequestModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun listAllOutgoingRequestsUntil(userId: String, filterName: String?, lastId: String): Single<List<ConnectionRequestModel>> {
        return databaseApi.listAllOutgoingRequestsUntil(userId, filterName, lastId)
                .map {
                    val models = ArrayList<ConnectionRequestModel>()
                    it.forEach {
                        models.add(
                                it.toDomainModel()
                        )
                    }
                    return@map models
                }
    }

    override fun updateProfile(profile: UserModel): Completable {
        return remoteApi.updateProfile(UpdateProfileRequest(
                profile.displayName!!,
                profile.photoUrl!!
        ), profile.id!!)
    }

    override fun updateGcmToken(gcmToken: String): Completable {
        return remoteApi.updateGcmToken(UpdateGcmTokenRequest(gcmToken))
    }

    override fun sendLocationRequest(to: String): Completable {
        return remoteApi.requestLocation(RequestLocationRequest(to))
    }

    override fun uploadLocation(location: LocationModel): Completable {
        return remoteApi.sendLocation(UploadLocationRequest(
                location.toId,
                location.public,
                location.latitude,
                location.longitude,
                location.timestamp
        ))
    }

    override fun updateConnection(newLevel: Int, userId: String): Completable {
        return remoteApi.updateConnection(
                UpdateConnectionRequest(newLevel, userId)
        )
    }

    override fun deleteConnection(userId: String): Completable {
        return remoteApi.unFriend(UnFriendRequest(userId))
    }

    override fun sendConnectionRequest(friendId: String): Single<ConnectionRequestModel> {
        return remoteApi.sendRequest(ConnectionRequest(friendId))
                .map {
                    it.result?.toDomainModel()
                }
    }

    override fun cancelConnectionRequest(requestId: String): Completable {
        return remoteApi.cancelRequest(requestId)
    }

    override fun acceptConnectionRequest(requestId: String): Single<ConnectionModel> {
        return remoteApi.acceptRequest(requestId)
                .map {
                    it.result.toDomainModel()
                }
    }

    override fun declineConnectionRequest(requestId: String): Completable {
        return remoteApi.declineRequest(requestId)
    }

}