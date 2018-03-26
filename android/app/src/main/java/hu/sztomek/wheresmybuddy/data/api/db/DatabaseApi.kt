package hu.sztomek.wheresmybuddy.data.api.db

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import hu.sztomek.wheresmybuddy.data.api.db.model.*
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.Single
import javax.inject.Inject

class DatabaseApi @Inject constructor(private val database: FirebaseFirestore) : IDatabaseApi {

    object ModelConverter {
        inline fun <reified T> handleQuerySuccess(result: QuerySnapshot, helper: IdCopyHelper<T>): List<T> {
            val dbModels = mutableListOf<T>()
            result.forEach({
                dbModels.add(handleDocumentSuccess(it, helper))
            })
            return dbModels.toList()
        }

        inline fun <reified T> handleDocumentSuccess(result: DocumentSnapshot, helper: IdCopyHelper<T>): T {
            val dbModel = result.toObject(T::class.java)
            return helper.copyWithId(result.id, dbModel)
        }
    }

    override fun getProfile(profileId: String): Single<UserDbModel> {
        return Single.create({ emitter ->
            database.document("users/$profileId")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                emitter.onSuccess(ModelConverter.handleDocumentSuccess(it.result, UserDbModel))
                            } else {
                                emitter.onError(DatabaseException("User doesn't exits: [$profileId]"))
                            }
                        } else emitter.onError(it.exception
                                ?: DatabaseException("Failed to get user profile with: profileId = [$profileId]"))
                    }
        })
    }

    private fun Query.textSearch(column: String, keyword: String?): Query {
        return if (keyword == null || keyword.trim().isEmpty()) {
            this
        } else {
            this.whereGreaterThanOrEqualTo(column, keyword)
                    .whereLessThan(column, keyword.substring(0, keyword.length - 2) + keyword.toCharArray()[keyword.length - 1].inc().toString())
        }
    }

    override fun searchProfilesAfter(keyword: String, lastId: String?, limit: Int): Single<List<UserDbModel>> {
        return Single.create({ emitter ->
            val query = database.collection("users").textSearch("displayName", keyword)
                    .orderBy("displayName")
                    .limit(limit.toLong())
            if (lastId != null) {
                database.collection("users")
                        .document(lastId)
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful && it.result.exists()) {
                                query.startAfter(it.result)
                                        .get()
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, UserDbModel))
                                            else emitter.onError(it.exception
                                                    ?: DatabaseException("Failed to search profiles with params: keyword = [$keyword], lastId = [$lastId], limit = [$limit]"))
                                        }
                            } else {
                                emitter.onError(it.exception
                                        ?: DatabaseException("Failed to search profiles with params: keyword = [$keyword], lastId = [$lastId], limit = [$limit]: lastId doesn't exist"))
                            }
                        }
            } else {
                query.get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, UserDbModel))
                            else emitter.onError(it.exception
                                    ?: DatabaseException("Failed to search profiles with params: keyword = [$keyword], lastId = [$lastId], limit = [$limit]"))
                        }
            }
        })
    }

    override fun searchAllProfilesUntil(keyword: String, lastId: String): Single<List<UserDbModel>> {
        return Single.create({ emitter ->
            database.collection("users")
                    .document(lastId)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful && it.result.exists()) {
                            database.collection("users")
                                    .textSearch("displayName", keyword)
                                    .orderBy("displayName")
                                    .endAt(it.result)
                                    .get()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, UserDbModel))
                                        else emitter.onError(it.exception
                                                ?: DatabaseException("Failed to search profiles with params: keyword = [$keyword], lastId = [$lastId]"))
                                    }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to search profiles with params: keyword = [$keyword], lastId = [$lastId]"))
                        }
                    }
        })
    }

    override fun getPendingConnectionBetween(user1: String, user2: String): Maybe<ConnectionRequestDbModel> {
        return Maybe.merge(
                Maybe.create { emitter: MaybeEmitter<ConnectionRequestDbModel> ->
                    database.collection("connectionRequests")
                            .whereEqualTo("from", user1)
                            .whereEqualTo("to", user2)
                            .limit(1L)
                            .get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val results = ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel)
                                    if (results.isEmpty()) {
                                        emitter.onComplete()
                                    } else {
                                        emitter.onSuccess(results[0])
                                    }
                                } else {
                                    emitter.onError(it.exception
                                            ?: DatabaseException("Failed to get pending connection between from [$user1] and to [$user2]"))
                                }
                            }
                },
                Maybe.create { emitter: MaybeEmitter<ConnectionRequestDbModel> ->
                    database.collection("connectionRequests")
                            .whereEqualTo("from", user2)
                            .whereEqualTo("to", user1)
                            .limit(1L)
                            .get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val results = ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel)
                                    if (results.isEmpty()) {
                                        emitter.onComplete()
                                    } else {
                                        emitter.onSuccess(results[0])
                                    }
                                } else {
                                    emitter.onError(it.exception
                                            ?: DatabaseException("Failed to get pending connection between from [$user2] and to [$user1]"))
                                }
                            }
                }).firstElement()
    }

    override fun getConnectionBetween(user1: String, user2: String): Maybe<ConnectionDbModel> {
        return Maybe.create { emitter: MaybeEmitter<ConnectionDbModel> ->
            database.document("users/$user1/connections/$user2")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                emitter.onSuccess(ModelConverter.handleDocumentSuccess(it.result, ConnectionDbModel))
                            } else {
                                emitter.onComplete()
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get connection between user1 [$user1] and user2 [$user2]"))
                        }
                    }
        }
    }

    override fun getLastLocation(userId: String, selfUserId: String): Maybe<LocationDbModel> {
        val latestPublic: Maybe<LocationDbModel> = Maybe.create { emitter ->
            database.collection("locations")
                    .whereEqualTo("from", userId)
                    .whereEqualTo("public", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1L)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.isEmpty.not()) {
                                emitter.onSuccess(ModelConverter.handleDocumentSuccess(it.result.documents[0], LocationDbModel))
                            } else {
                                emitter.onComplete()
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get public location from [$userId]"))
                        }
                    }
        }

        val latestPrivate: Maybe<LocationDbModel> = Maybe.create { emitter ->
            database.collection("locations")
                    .whereEqualTo("from", userId)
                    .whereEqualTo("to", selfUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1L)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.isEmpty.not()) {
                                emitter.onSuccess(ModelConverter.handleDocumentSuccess(it.result.documents[0], LocationDbModel))
                            } else {
                                emitter.onComplete()
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get private location from [$userId] to [$selfUserId]"))
                        }
                    }
        }

        return Maybe.merge(latestPublic, latestPrivate)
                .sorted { public, private ->
                    val publicTimestamp = public.timestamp ?: -1L
                    val privateTimestamp = private.timestamp ?: -1L
                    when {
                        publicTimestamp > privateTimestamp -> 1
                        publicTimestamp == privateTimestamp -> 0
                        else -> -1
                    }
                }
                .firstElement()
    }

    override fun getLocation(locationId: String): Single<LocationDbModel> {
        return Single.create { emitter ->
            database.document("locations/$locationId")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                emitter.onSuccess(ModelConverter.handleDocumentSuccess(it.result, LocationDbModel))
                            } else {
                                emitter.onError(DatabaseException("Location [$locationId] doesn't exist!"))
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Couldn't get location [$locationId]"))
                        }
                    }
        }
    }

    override fun listConnections(profileId: String, nameFilter: String?, lastId: String?, trustedOnly: Boolean, limit: Int): Single<List<ConnectionDbModel>> {
        return Single.create { emitter ->
            var query = database.collection("users/$profileId/connections")
                    .textSearch("displayName", nameFilter)
                    .orderBy("displayName")
                    .limit(limit.toLong())
            if (trustedOnly) {
                query = query.whereEqualTo("level", if (trustedOnly) 1 else 0)
            }
            if (lastId != null) {
                database.document("users/$profileId/connections/$lastId")
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful && it.result.exists()) {
                                query.startAfter(it.result)
                                        .get()
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionDbModel))
                                            } else {
                                                emitter.onError(it.exception
                                                        ?: DatabaseException("Failed to list connection  with params: userId [$profileId], lastId [$lastId], nameFilter [$nameFilter]"))
                                            }
                                        }
                            } else {
                                emitter.onError(it.exception
                                        ?: DatabaseException("Failed to get connection for lastId [$lastId]"))
                            }
                        }
            } else {
                query.get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionDbModel))
                            } else {
                                emitter.onError(it.exception
                                        ?: DatabaseException("Failed to list connection  with params: userId [$profileId], lastId [$lastId], nameFilter [$nameFilter]"))
                            }
                        }
            }
        }
    }

    override fun listAllConnectionsUntil(profileId: String, nameFilter: String?, lastId: String, trustedOnly: Boolean): Single<List<ConnectionDbModel>> {
        return Single.create { emitter ->
            database.document("users/$profileId/connections/$lastId")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful && it.result.exists()) {
                            var query = database.collection("users/$profileId/connections")
                                    .textSearch("displayName", nameFilter)
                                    .orderBy("displayName")
                                    .endAt(it.result)
                            if (trustedOnly) {
                                query = query.whereEqualTo("level", 1)
                            }
                            query.get()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionDbModel))
                                        } else {
                                            emitter.onError(it.exception
                                                    ?: DatabaseException("Failed to get connections until with params: profileId [$profileId], nameFilter [$nameFilter], lastId [$lastId]"))
                                        }
                                    }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get connection with lastId [$lastId]"))
                        }
                    }
        }
    }

    override fun listIncomingRequests(profileId: String, nameFilter: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestDbModel>> {
        return Single.create { emitter ->
            val function: (Task<QuerySnapshot>) -> Unit = {
                if (it.isSuccessful) {
                    emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel))
                } else {
                    emitter.onError(it.exception
                            ?: DatabaseException("Failed to get incoming requests with params: profileId [$profileId], nameFilter = [$nameFilter], lastId = [$lastId], limit = [$limit]"))
                }
            }
            val query = database.collection("connectionRequests")
                    .whereEqualTo("to", profileId)
                    .textSearch("fromDisplayName", nameFilter)
                    .orderBy("fromDisplayName")
                    .limit(limit.toLong())
            if (!lastId.isNullOrBlank()) {
                database.document("connectionRequests/$lastId")
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (it.result.exists()) {
                                    query.startAfter(it.result)
                                            .get()
                                            .addOnCompleteListener(function)
                                } else {
                                    emitter.onError(DatabaseException("ConnectionRequest [$lastId] doesn't exist"))
                                }
                            } else {
                                emitter.onError(it.exception
                                        ?: DatabaseException("Failed to get user by id: [$lastId]"))
                            }
                        }
            } else {
                query.get()
                        .addOnCompleteListener(function)
            }
        }
    }

    override fun listAllIncomingRequestsUntil(profileId: String, nameFilter: String?, lastId: String): Single<List<ConnectionRequestDbModel>> {
        return Single.create { emitter ->
            database.document("connectionRequests/$lastId")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                database.collection("connectionRequests")
                                        .whereEqualTo("to", profileId)
                                        .textSearch("fromDisplayName", nameFilter)
                                        .orderBy("fromDisplayName")
                                        .endAt(it.result)
                                        .get()
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel))
                                            } else {
                                                emitter.onError(it.exception
                                                        ?: DatabaseException("Failed to get incoming connection requests with params: profileId = [$profileId], nameFilter = [$nameFilter], lastId = [$lastId]"))
                                            }
                                        }
                            } else {
                                emitter.onError(DatabaseException("ConnectionRequest [$lastId] doesn't exist"))
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get user [$lastId]"))
                        }
                    }
        }
    }

    override fun listOutgoingRequests(profileId: String, nameFilter: String?, lastId: String?, limit: Int): Single<List<ConnectionRequestDbModel>> {
        return Single.create { emitter ->
            val function: (Task<QuerySnapshot>) -> Unit = {
                if (it.isSuccessful) {
                    emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel))
                } else {
                    emitter.onError(it.exception
                            ?: DatabaseException("Failed to get outgoing requests with params: profileId [$profileId], nameFilter = [$nameFilter], lastId = [$lastId], limit = [$limit]"))
                }
            }
            val query = database.collection("connectionRequests")
                    .whereEqualTo("from", profileId)
                    .textSearch("toDisplayName", nameFilter)
                    .orderBy("toDisplayName")
                    .limit(limit.toLong())
            if (!lastId.isNullOrBlank()) {
                database.document("connectionRequests/$lastId")
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (it.result.exists()) {
                                    query.startAfter(it.result)
                                            .get()
                                            .addOnCompleteListener(function)
                                } else {
                                    emitter.onError(DatabaseException("ConnectionRequest [$lastId] doesn't exist"))
                                }
                            } else {
                                emitter.onError(it.exception
                                        ?: DatabaseException("Failed to get user by id: [$lastId]"))
                            }
                        }
            } else {
                query.get()
                        .addOnCompleteListener(function)
            }
        }
    }

    override fun listAllOutgoingRequestsUntil(profileId: String, nameFilter: String?, lastId: String): Single<List<ConnectionRequestDbModel>> {
        return Single.create { emitter ->
            database.document("connectionRequests/$lastId")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                database.collection("connectionRequests")
                                        .whereEqualTo("from", profileId)
                                        .textSearch("toDisplayName", nameFilter)
                                        .orderBy("toDisplayName")
                                        .endAt(it.result)
                                        .get()
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                emitter.onSuccess(ModelConverter.handleQuerySuccess(it.result, ConnectionRequestDbModel))
                                            } else {
                                                emitter.onError(it.exception
                                                        ?: DatabaseException("Failed to get outgoing connection requests with params: profileId = [$profileId], nameFilter = [$nameFilter], lastId = [$lastId]"))
                                            }
                                        }
                            } else {
                                emitter.onError(DatabaseException("ConnectionRequest [$lastId] doesn't exist"))
                            }
                        } else {
                            emitter.onError(it.exception
                                    ?: DatabaseException("Failed to get user [$lastId]"))
                        }
                    }
        }
    }

}
