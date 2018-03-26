package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.ImageUploader
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import io.reactivex.Single
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource, private val imageUpload: ImageUploader) {

    fun execute(action: Action.UpdateProfileAction): Single<Result> {
        return if (action.photoPath!!.startsWith("http")) {
            val userModel = UserModel(action.userId, action.displayName, null, action.photoPath)
            datasource.updateProfile(
                    userModel
            ).toSingleDefault(Result.complete(action, userModel))
        } else {
            imageUpload.uploadImageFromFile(action.userId, action.photoPath)
                    .observeOn(appSchedulers.io)
                    .flatMap {
                        val userModel = UserModel(action.userId, action.displayName, null, it)
                        datasource.updateProfile(
                                userModel
                        ).toSingleDefault(Result.complete(action, userModel))
                    }
        }
    }

}