package hu.sztomek.wheresmybuddy.data.di

import dagger.Binds
import dagger.Module
import hu.sztomek.wheresmybuddy.data.api.db.DatabaseApi
import hu.sztomek.wheresmybuddy.data.api.db.IDatabaseApi
import hu.sztomek.wheresmybuddy.data.api.user.IUserApi
import hu.sztomek.wheresmybuddy.data.api.user.UserApi
import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.data.Datasource

@Module
interface ApiModule {

    @Binds
    fun provideUserManager(api: UserApi): IUserApi

    @Binds
    fun provideDatabaseApi(api: DatabaseApi): IDatabaseApi

    @Binds
    fun provideRemoteDataSource(dataSource: Datasource): IDatasource
}