package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.common

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.EditText
import butterknife.BindView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.*
import hu.sztomek.wheresmybuddy.presentation.common.adapter.BaseRecyclerViewAdapter
import hu.sztomek.wheresmybuddy.presentation.common.adapter.EndlessScrollListener
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.IdProvider
import hu.sztomek.wheresmybuddy.presentation.screens.landing.TitleProvider
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.PageSelectedListener
import hu.sztomek.wheresmybuddy.presentation.view.LoadingErrorConstraintLayout
import hu.sztomek.wheresmybuddy.presentation.view.SwipeRefreshEmptyView
import javax.inject.Inject

abstract class BasePeopleFragment<M : Parcelable, VM : BaseViewModel<M>> : BaseFragment<M, VM>(), Injectable, TitleProvider, PageSelectedListener {

    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @BindView(R.id.etFilterName)
    lateinit var etFilterName: EditText
    @BindView(R.id.btnFilter)
    lateinit var btnFilter: Button
    @BindView(R.id.leContainer)
    lateinit var leContainer: LoadingErrorConstraintLayout
    @BindView(R.id.sreFriends)
    lateinit var sreFriends: SwipeRefreshEmptyView
    @BindView(R.id.rvList)
    lateinit var rvFriends: RecyclerView

    private var lastModel: M? = null
    private var endlessScrollListener: EndlessScrollListener? = null

    override fun getPersistableUiState(): M {
        return lastModel!!
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun handleStateChange(state: State<M>?) {
        state?.let {
            leContainer.state = when {
                state.loading -> LoadingErrorConstraintLayout.STATE_LOADING
                state.error != null -> {
                    sreFriends.showEmptyView = false
                    leContainer.errorMessage = state.error.message
                    LoadingErrorConstraintLayout.STATE_ERROR
                }
                else -> {
                    sreFriends.showEmptyView = Helpers.safeCastTo<ListData<*>>(it.data)?.results?.isEmpty() == true
                    LoadingErrorConstraintLayout.STATE_IDLE
                }
            }
            it.data?.let {
                if (it is ListData<*>) {
                    adapter.setData(it.results)
                }
                lastModel = it
            }
            sreFriends.getSwipeRefreshLayout().isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sreFriends.getSwipeRefreshLayout().setOnRefreshListener {
            endlessScrollListener?.reset()
            triggerSearch(etFilterName.text.toString(), null, true) }
        rvFriends.layoutManager = layoutManager
        rvFriends.adapter = adapter
        endlessScrollListener = EndlessScrollListener(layoutManager as LinearLayoutManager, {
            adapter.getItems().lastOrNull {
                it is IdProvider
            }?.let {
                        loadMore(etFilterName.text.toString(), Helpers.safeCastTo<IdProvider>(it)?.id)
                    }
        })
        rvFriends.addOnScrollListener(endlessScrollListener)

        btnFilter.setOnClickListener { _ ->
            endlessScrollListener?.reset()
            triggerSearch(etFilterName.text.toString(), null, true)
        }
    }

    override fun onDestroyView() {
        adapter.setData(emptyList())
        rvFriends.layoutManager = null
        rvFriends.clearOnScrollListeners()
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        onSelected()
    }

    override fun onSelected() {
        endlessScrollListener?.reset()
        triggerSearch(etFilterName.text.toString(), null, true)
    }

    abstract fun triggerSearch(nameFilter: String?, lastId: String?, fromLastId: Boolean)
    abstract fun loadMore(nameFilter: String?, lastId: String?)
    abstract var adapter: BaseRecyclerViewAdapter
}