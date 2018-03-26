package hu.sztomek.wheresmybuddy.data.api.user

import hu.sztomek.wheresmybuddy.data.api.user.model.UserAuthModel
import io.reactivex.Completable
import io.reactivex.Single

interface IUserApi {

    fun hasActiveUser(): Single<Boolean>
    fun getUser(): Single<UserAuthModel>
    fun getAuthenticationToken(forceRefresh: Boolean): Single<String>
    fun logout(): Completable
    fun deleteAccount(): Completable

}