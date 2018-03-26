package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import java.text.DateFormat

@Module
class FriendsModule {

    @Provides
    fun provideLayoutManager(context: Context): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }

    @Provides
    fun provideAdapter(dateFormat: DateFormat, imageLoader: ImageLoader): FriendsAdapter {
        return FriendsAdapter(dateFormat, imageLoader)
    }

}