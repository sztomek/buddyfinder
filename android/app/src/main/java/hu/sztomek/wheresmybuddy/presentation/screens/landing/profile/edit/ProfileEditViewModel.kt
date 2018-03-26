package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import hu.sztomek.wheresmybuddy.domain.usecase.DeleteAccountUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.GetProfileUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.UpdateProfileUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.UiError
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class ProfileEditViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val getProfileUseCase: GetProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase
) : BaseViewModel<ProfileModel>(
        appSchedulers,
        application,
        State.idleStateWithData(ProfileModel(null, null, null))
) {

    override fun updateState(current: State<ProfileModel>, result: Result): State<ProfileModel> {
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                error = when(result.error) {
                    null -> null
                    is UiError.FieldValidationError -> result.error
                    else -> UiError.GeneralUiError(result.error.toString())
                },
                data = when(result.data) {
                    null -> current.data
                    is UserModel -> result.data.toUiModel()
                    is BooleanModel -> if (result.data.value) null else current.data
                    else -> current.data
                }
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        return ObservableTransformer {
            upstream ->
                Observable.merge(
                        upstream.ofType(Action.GetProfileAction::class.java)
                                .flatMap {
                                    action ->
                                        getProfileUseCase.execute(action)
                                                .toObservable()
                                                .onErrorReturn { Result.error(action, it)}
                                                .startWith(Result.inProgress(action))
                                },
                        upstream.ofType(Action.UpdateProfileAction::class.java)
                                .flatMap {
                                    action ->
                                        if (action.displayName.isNullOrBlank()) {
                                            Observable.just(Result.error(action, UiError.FieldValidationError("Name field cannot be empty!")))
                                        } else {
                                            updateProfileUseCase.execute(action)
                                                    .toObservable()
                                                    .onErrorReturn { Result.error(action, it) }
                                                    .startWith(Result.inProgress(action))
                                        }
                                },
                        upstream.ofType(Action.DeleteAccountAction::class.java)
                                .flatMap {
                                    action ->
                                        deleteAccountUseCase.execute(action)
                                                .toObservable()
                                                .startWith(Result.inProgress(action))
                                                .onErrorReturn { Result.complete(action, BooleanModel(true)) } // complete even on error
                                }
                )
        }
    }

    fun updateProfile(userModel: ProfileModel) {
        actions.onNext(Action.UpdateProfileAction(userModel.id!!, userModel.displayName, userModel.profilePicture))
    }

    fun deleteAccount() {
        actions.onNext(Action.DeleteAccountAction())
    }

    fun getProfile(id: String) {
        actions.onNext(Action.GetProfileAction(id))
    }
}