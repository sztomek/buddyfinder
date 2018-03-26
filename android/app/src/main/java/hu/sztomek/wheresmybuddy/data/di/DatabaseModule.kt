package hu.sztomek.wheresmybuddy.data.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

}