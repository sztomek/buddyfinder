package hu.sztomek.wheresmybuddy.data.api.user

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import hu.sztomek.wheresmybuddy.data.api.user.model.UserAuthModel
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Single
import javax.inject.Inject

class UserApi @Inject constructor(private val auth: FirebaseAuth, private val context: Context) : IUserApi {

    override fun hasActiveUser(): Single<Boolean> {
        return Single.fromCallable({
            return@fromCallable auth.currentUser != null
        })
    }

    override fun getUser(): Single<UserAuthModel> {
        return Single.fromCallable({
            val currentUser = auth.currentUser
            currentUser?.let {
                return@fromCallable UserAuthModel(currentUser.uid, currentUser.displayName, currentUser.email, currentUser.photoUrl.toString())
            }
        })
    }

    override fun getAuthenticationToken(forceRefresh: Boolean): Single<String> {
        return Single.fromCallable({
            auth.currentUser?.let {
                return@fromCallable it.getIdToken(forceRefresh).result.token
            }
        })
    }

    override fun logout(): Completable {
        return Completable.create({ emitter ->
            AuthUI.getInstance().signOut(context)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            emitter.onComplete()
                        } else {
                            emitter.onError(it.exception ?: UserException("Failed to run AuthUI.singOut"))
                        }
                    }
        })
    }

    override fun deleteAccount(): Completable {
        return Completable.create { emitter ->
            auth.currentUser?.let {
                user ->
                    user.delete()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                emitter.onComplete()
                            } else {
                                emitter.onError(it.exception ?: UserException("Failed to delete User [$user]"))
                            }
                        }
            }
        }
    }
}