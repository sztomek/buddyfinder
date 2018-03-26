package hu.sztomek.wheresmybuddy.presentation.screens.landing.people

import android.content.res.Resources
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends.FriendsFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming.IncomingRequestsFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing.OutgoingRequestsFragment

@Module
class PeopleModule {

    @Provides
    fun provideChildFragmentManager(fragment: PeopleFragment): FragmentManager {
        return fragment.childFragmentManager
    }

    @Provides
    fun providePagerAdapter(fragmentManager: FragmentManager, resources: Resources): FragmentPagerAdapter {
        return PeoplePagerAdapter(fragmentManager, listOf(FriendsFragment(), IncomingRequestsFragment(), OutgoingRequestsFragment()), resources)
    }

}