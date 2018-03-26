package hu.sztomek.wheresmybuddy.presentation.common

interface ListData<out T: Any> {

    val results: List<T>

}