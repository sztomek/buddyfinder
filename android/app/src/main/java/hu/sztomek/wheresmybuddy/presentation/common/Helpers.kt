package hu.sztomek.wheresmybuddy.presentation.common

class Helpers {

    companion object {

        inline fun <reified T> safeCastTo(input: Any?): T? {
            return input as? T
        }

    }

}