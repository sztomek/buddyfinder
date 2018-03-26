package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming

import android.os.Bundle
import android.view.View
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.model.persistable.PendingRequestsModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.common.BasePeopleFragment
import javax.inject.Inject

class IncomingRequestsFragment : BasePeopleFragment<PendingRequestsModel, IncomingRequestsViewModel>() {

    @Inject
    lateinit var incomingRequestsAdapter: IncomingRequestsAdapter
    @Inject
    lateinit var router: IRouter

    override var adapter: BaseRecyclerViewAdapter
        get() = incomingRequestsAdapter
        set(value) {}

    override val titleRes: Int
        get() = R.string.tab_incoming

    override fun getLayoutId(): Int {
        return R.layout.fragment_incoming_requests
    }

    override fun getViewModelClass(): Class<IncomingRequestsViewModel> {
        return IncomingRequestsViewModel::class.java
    }

    override fun triggerSearch(nameFilter: String?, lastId: String?, fromLastId: Boolean) {
        viewModel.triggerSearch(nameFilter, lastId, fromLastId)
    }

    override fun loadMore(nameFilter: String?, lastId: String?) {
        viewModel.loadMore(nameFilter, lastId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        incomingRequestsAdapter.clickListener = object: IncomingRequestsAdapter.IncomingRequestClickListener {
            override fun onProfileClicked(userId: String) {
                router.toProfile(userId)
            }

            override fun onAcceptClicked(requestId: String) {
                viewModel.acceptRequest(requestId)
            }

            override fun onDeclineClicked(requestId: String) {
                viewModel.declineRequest(requestId)
            }
        }
    }
}