package hu.sztomek.wheresmybuddy.domain.common

import hu.sztomek.wheresmybuddy.domain.action.Action

data class Result constructor(
        val state: ResultState
        , val action: Action
        , val error: Throwable?
        , val data: Entity?
) {
    companion object {
        fun inProgress(action: Action) = Result(ResultState.IN_PROGRESS, action, null, null)
        fun error(action: Action, error: Throwable) = Result(ResultState.FINISHED, action, error, null)
        fun complete(action: Action, data: Entity) = Result(ResultState.FINISHED, action, null, data)
    }

}