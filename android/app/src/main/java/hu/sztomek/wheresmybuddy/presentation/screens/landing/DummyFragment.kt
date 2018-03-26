package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hu.sztomek.wheresmybuddy.R

class DummyFragment : Fragment(), TitleProvider {

    override val titleRes: Int
        get() = R.string.app_name

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dummy, container!!, false)
    }

}