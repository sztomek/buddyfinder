package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.device.media.ImageLoader

@Module
class SearchModule {

    @Provides
    fun provideLayoutManager(context: Context): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }

    @Provides
    fun provideAdapter(fragment: SearchFragment, imageLoader: ImageLoader): SearchAdapter {
        return SearchAdapter(fragment, imageLoader)
    }

}