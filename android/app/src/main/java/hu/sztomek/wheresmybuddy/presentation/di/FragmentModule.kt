package hu.sztomek.wheresmybuddy.presentation.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hu.sztomek.wheresmybuddy.device.di.DeviceModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.discover.DiscoverFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.discover.DiscoverModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.PeopleFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.PeopleModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends.FriendsFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends.FriendsModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming.IncomingRequestsFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming.IncomingRequestsModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing.OutgoingRequestsFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing.OutgoingRequestsModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.search.SearchFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.search.SearchModule

@Module
interface FragmentModule {

    @ContributesAndroidInjector(modules = arrayOf(
            SearchModule::class
    ))
    fun bindSearch(): SearchFragment

    @ContributesAndroidInjector(modules = arrayOf(
            PeopleModule::class,
            DeviceModule::class
    ))
    fun bindPeople(): PeopleFragment

    @ContributesAndroidInjector(modules = arrayOf(
            DateFormatterModule::class,
            FriendsModule::class
    ))
    fun bindFriends(): FriendsFragment

    @ContributesAndroidInjector(modules = arrayOf(
            DateFormatterModule::class,
            IncomingRequestsModule::class
    ))
    fun bindIncoming(): IncomingRequestsFragment

    @ContributesAndroidInjector(modules = arrayOf(
            DateFormatterModule::class,
            OutgoingRequestsModule::class
    ))
    fun bindOutgoing(): OutgoingRequestsFragment

    @ContributesAndroidInjector(modules = arrayOf(
            DiscoverModule::class
    ))
    fun bindDiscover(): DiscoverFragment



}