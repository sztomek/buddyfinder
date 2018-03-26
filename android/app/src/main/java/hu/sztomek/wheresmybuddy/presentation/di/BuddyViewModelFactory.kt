package hu.sztomek.wheresmybuddy.presentation.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class BuddyViewModelFactory @Inject constructor(private var providerMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        var provider = providerMap[modelClass]
        if (provider == null) {
            for (entry: kotlin.collections.Map.Entry<Class<out ViewModel>, Provider<ViewModel>> in providerMap.entries) {
                if (modelClass.isAssignableFrom(entry.key)) {
                    provider = entry.value
                    break
                }
            }
        }
        if (provider == null) {
            throw IllegalArgumentException("Unknown model class $modelClass")
        }

        return provider.get() as T
    }
}