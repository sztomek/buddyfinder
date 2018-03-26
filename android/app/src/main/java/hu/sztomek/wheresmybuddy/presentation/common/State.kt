package hu.sztomek.wheresmybuddy.presentation.common

data class State<D>(val loading: Boolean, val error: UiError?, val data: D?) {

    companion object {
        fun <D> idleStateWithData(data: D?): State<D> {
            return State(false, null, data)
        }
    }

}