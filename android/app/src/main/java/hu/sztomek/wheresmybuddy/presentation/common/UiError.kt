package hu.sztomek.wheresmybuddy.presentation.common

sealed class UiError(val recoverable: Boolean, message: String) : Throwable(message){

    class GeneralUiError(message: String) : UiError(false, message)
    class FieldValidationError(message: String) : UiError(true, message)

    override fun toString(): String {
        return "UiError(recoverable=$recoverable, ${super.toString()})"
    }


}