package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class DiscoverModule {

    @Provides
    fun provideImageCache(): MarkerImageCache {
        return MarkerImageCacheImpl()
    }

    @Provides
    fun provideInfoWindowAdapter(context: Context): InfoWindowAdapter {
        return InfoWindowAdapter(context)
    }

}