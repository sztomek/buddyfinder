package hu.sztomek.wheresmybuddy.presentation.screens.landing

import dagger.Module
import dagger.Provides

@Module
class StatusUiModule {

    @Provides
    fun provideStatusSpinnerAdapter(): StatusSpinnerAdapter {
        return StatusSpinnerAdapter(listOf(StatusSpinnerItem.OfflineStatusSpinnerItem(), StatusSpinnerItem.OnlineStatusSpinnerItem()))
    }

}