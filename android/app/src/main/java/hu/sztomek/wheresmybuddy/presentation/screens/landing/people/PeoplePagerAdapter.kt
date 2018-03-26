package hu.sztomek.wheresmybuddy.presentation.screens.landing.people

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.TitleProvider

class PeoplePagerAdapter(manager: FragmentManager, private val fragments: List<Fragment>, private val resources: Resources) : FragmentPagerAdapter(manager) {

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence = resources.getString((fragments[position] as TitleProvider).titleRes)
}