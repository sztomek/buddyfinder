package hu.sztomek.wheresmybuddy.data.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides

@Module
class UserModule {

    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

}