package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import butterknife.BindView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.model.persistable.FriendsModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.common.BasePeopleFragment
import javax.inject.Inject

class FriendsFragment : BasePeopleFragment<FriendsModel, FriendsViewModel>() {

    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var friendsAdapter: FriendsAdapter

    @BindView(R.id.cbTrusted)
    lateinit var cbTrusted: CheckBox

    override var adapter: BaseRecyclerViewAdapter
        get() = friendsAdapter
        set(value) {}

    override val titleRes: Int
        get() = R.string.tab_friends

    override fun getLayoutId(): Int {
        return R.layout.fragment_friends
    }

    override fun getViewModelClass(): Class<FriendsViewModel> {
        return FriendsViewModel::class.java
    }

    override fun triggerSearch(nameFilter: String?, lastId: String?, fromLastId: Boolean) {
        viewModel.listFriends(nameFilter, lastId, fromLastId, cbTrusted.isChecked)
    }

    override fun loadMore(nameFilter: String?, lastId: String?) {
        viewModel.loadMore(nameFilter, lastId, cbTrusted.isChecked)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        friendsAdapter.clickListener = object: FriendsAdapter.FriendClickListener {
            override fun onProfileClicked(userId: String) {
                router.toProfile(userId)
            }

            override fun onLocateClicked(userId: String) {
                viewModel.locate(userId)
            }
        }
    }

    override fun handleStateChange(state: State<FriendsModel>?) {
        super.handleStateChange(state)
        state?.let {
            cbTrusted.isChecked = it.data?.trustedOnly == true
        }
    }
}
