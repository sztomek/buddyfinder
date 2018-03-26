package hu.sztomek.wheresmybuddy.presentation.screens.landing.people

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.screens.landing.TitleProvider
import javax.inject.Inject

class PeopleFragment : Fragment(), Injectable, TitleProvider {

    @Inject
    lateinit var adapter: FragmentPagerAdapter

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override val titleRes: Int
        get() = R.string.label_my_connections

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tlTabs)

        viewPager = view.findViewById(R.id.vpFragments)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Helpers.safeCastTo<PageSelectedListener>(adapter.getItem(position))?.onSelected()
            }
        })
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
    }
}