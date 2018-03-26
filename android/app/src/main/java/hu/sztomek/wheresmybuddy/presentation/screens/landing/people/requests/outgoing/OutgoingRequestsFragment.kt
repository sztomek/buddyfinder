package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing

import android.os.Bundle
import android.view.View
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.model.persistable.PendingRequestsModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.common.BasePeopleFragment
import javax.inject.Inject

class OutgoingRequestsFragment : BasePeopleFragment<PendingRequestsModel, OutgoingRequestsViewModel>() {

    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var outgoingRequestsAdapter: OutgoingRequestsAdapter

    override var adapter: BaseRecyclerViewAdapter
        get() = outgoingRequestsAdapter
        set(value) {}

    override val titleRes: Int
        get() = R.string.tab_outgoing

    override fun getLayoutId(): Int {
        return R.layout.fragment_incoming_requests
    }

    override fun getViewModelClass(): Class<OutgoingRequestsViewModel> {
        return OutgoingRequestsViewModel::class.java
    }

    override fun triggerSearch(nameFilter: String?, lastId: String?, fromLastId: Boolean) {
        viewModel.triggerSearch(nameFilter, lastId, fromLastId)
    }

    override fun loadMore(nameFilter: String?, lastId: String?) {
        viewModel.loadMore(nameFilter, lastId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outgoingRequestsAdapter.clickListener = object: OutgoingRequestsAdapter.OutgoingRequestClickListener {
            override fun onProfileClicked(profileId: String) {
                router.toProfile(profileId)
            }

            override fun onCancelClicked(requestId: String) {
                viewModel.cancelRequest(requestId)
            }
        }
    }

}