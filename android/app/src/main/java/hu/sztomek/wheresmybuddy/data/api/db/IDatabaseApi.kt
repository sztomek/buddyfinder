package hu.sztomek.wheresmybuddy.data.api.db

import hu.sztomek.wheresmybuddy.data.api.db.model.ConnectionDbModel
import hu.sztomek.wheresmybuddy.data.api.db.model.ConnectionRequestDbModel
import hu.sztomek.wheresmybuddy.data.api.db.model.LocationDbModel
import hu.sztomek.wheresmybuddy.data.api.db.model.UserDbModel
import io.reactivex.Maybe
import io.reactivex.Single

interface IDatabaseApi {

    fun getProfile(profileId: String): Single<UserDbModel>
    fun searchProfilesAfter(keyword: String, lastId: String?, limit: Int): Single<List<UserDbModel>>
    fun searchAllProfilesUntil(keyword: String, lastId: String): Single<List<UserDbModel>>
    fun getPendingConnectionBetween(user1: String, user2: String): Maybe<ConnectionRequestDbModel>
    fun getConnectionBetween(user1: String, user2: String): Maybe<ConnectionDbModel>
    fun getLastLocation(userId: String, selfUserId: String): Maybe<LocationDbModel>
    fun listConnections(profileId: String, nameFilter: String?, lastId: String?, trustedOnly: Boolean, limit: Int): Single<List<ConnectionDbModel>>
    fun listAllConnectionsUntil(profileId: String, nameFilter: String?, lastId: String, trustedOnly: Boolean): Single<List<ConnectionDbModel>>
    fun listIncomingRequests(profileId: String, nameFilter: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestDbModel>>
    fun listAllIncomingRequestsUntil(profileId: String, nameFilter: String?, lastId: String): Single<List<ConnectionRequestDbModel>>
    fun listOutgoingRequests(profileId: String, nameFilter: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestDbModel>>
    fun listAllOutgoingRequestsUntil(profileId: String, nameFilter: String?, lastId: String): Single<List<ConnectionRequestDbModel>>
    fun getLocation(locationId: String): Single<LocationDbModel>

}