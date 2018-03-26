package hu.sztomek.wheresmybuddy.device.di

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.BuildConfig
import hu.sztomek.wheresmybuddy.device.file.ImageResizer
import hu.sztomek.wheresmybuddy.device.file.ImageUploaderImpl
import hu.sztomek.wheresmybuddy.device.location.Location
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.device.media.ImageLoaderImpl
import hu.sztomek.wheresmybuddy.device.notification.Notifications
import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.INotifications
import hu.sztomek.wheresmybuddy.domain.ImageUploader
import hu.sztomek.wheresmybuddy.presentation.app.BuddyApp
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Singleton

@Module
class DeviceModule {

    @Provides
    fun provideNotificationManager(context: Context): NotificationManager? {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }

    @Provides
    fun provideNotifications(context: Context, manager: NotificationManager?, imageLoader: ImageLoader): INotifications {
        return Notifications(context, manager, imageLoader)
    }

    @Provides
    fun provideImageResizer(): ImageResizer {
        return ImageResizer(400)
    }


    @Provides
    fun provideStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun provideImageUploader(imageResizer: ImageResizer, firebaseStorage: FirebaseStorage): ImageUploader {
        return ImageUploaderImpl(imageResizer, firebaseStorage)
    }

    @Singleton
    @Provides
    fun providePicasso(context: Context, okHttpClient: OkHttpClient): Picasso {
        return Picasso.Builder(context)
                .downloader(OkHttp3Downloader(okHttpClient))
                .indicatorsEnabled(BuildConfig.DEBUG)
                .loggingEnabled(BuildConfig.DEBUG)
                .listener { _, uri, exception ->
                    Timber.d("Failed to load image [$uri]: $exception")
                }
                .build()
    }

    @Provides
    fun provideImageLoader(picasso: Picasso): ImageLoader {
        return ImageLoaderImpl(picasso)
    }

    @Provides
    fun provideResources(app: BuddyApp): Resources {
        return app.resources
    }

    @Singleton
    @Provides
    fun provideLocations(app: BuddyApp): ILocation {
        return Location(app.applicationContext)
    }

}