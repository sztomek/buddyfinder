package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import butterknife.BindView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.BaseFragment
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.adapter.EndlessScrollListener
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.IdProvider
import hu.sztomek.wheresmybuddy.presentation.model.persistable.SearchModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.TitleProvider
import hu.sztomek.wheresmybuddy.presentation.view.LoadingErrorConstraintLayout
import hu.sztomek.wheresmybuddy.presentation.view.SwipeRefreshEmptyView
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchModel, SearchViewModel>(), Injectable, TitleProvider, SearchAdapter.SearchAdapterClickListener {

    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var adapter: SearchAdapter
    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @BindView(R.id.leclRoot)
    lateinit var loadingErrorView: LoadingErrorConstraintLayout
    @BindView(R.id.srevContainer)
    lateinit var srevContainer: SwipeRefreshEmptyView
    @BindView(R.id.rvList)
    lateinit var rvSearchResults: RecyclerView
    @BindView(R.id.ibSearch)
    lateinit var ibSearch: ImageButton
    @BindView(R.id.etFilterName)
    lateinit var etFilter: EditText

    override val titleRes: Int
        get() = R.string.label_search

    private var latestModel: SearchModel? = null
    private var endlessScrollListener: EndlessScrollListener? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srevContainer.showEmptyView = true
        srevContainer.emptyMessage = getString(R.string.nothing_to_show)
        srevContainer.getSwipeRefreshLayout().setOnRefreshListener({
            triggerSearchAndHideKeyboard()
        })
        rvSearchResults.layoutManager = layoutManager
        rvSearchResults.adapter = adapter
        endlessScrollListener = EndlessScrollListener(layoutManager as LinearLayoutManager) {
            viewModel.loadMore(etFilter.text.toString(), (adapter.getItems().lastOrNull { it is IdProvider } as IdProvider).id)
        }
        rvSearchResults.addOnScrollListener(endlessScrollListener)

        ibSearch.setOnClickListener { _ ->
            triggerSearchAndHideKeyboard()
        }
        etFilter.setOnEditorActionListener { _: TextView?, actionId: Int?, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearchAndHideKeyboard()
                true
            } else false
        }
    }

    private fun triggerSearchAndHideKeyboard() {
        endlessScrollListener?.reset()
        viewModel.triggerSearch(etFilter.text.toString(), null, true)
        (etFilter.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(etFilter.windowToken, 0)
    }

    override fun getPersistableUiState(): SearchModel {
        return SearchModel(
                latestModel?.keyWord,
                latestModel?.lastId
        )
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<SearchViewModel> {
        return SearchViewModel::class.java
    }

    override fun handleStateChange(state: State<SearchModel>?) {
        latestModel = state?.data
        state?.let {
            loadingErrorView.errorMessage = state.error?.message
            loadingErrorView.state = if (state.loading) LoadingErrorConstraintLayout.STATE_LOADING else if (state.error != null) LoadingErrorConstraintLayout.STATE_ERROR else LoadingErrorConstraintLayout.STATE_IDLE
            if (state.data != null) {
                adapter.setData(state.data.results)
            }
            srevContainer.getSwipeRefreshLayout().isRefreshing = false
            srevContainer.showEmptyView = adapter.itemCount == 0 && state.error == null
            srevContainer.visibility = if (state.error == null) View.VISIBLE else View.GONE

        }
    }

    override fun onStart() {
        super.onStart()
        if (latestModel?.lastId != null) {
            endlessScrollListener?.reset()
            viewModel.triggerSearch(etFilter.text.toString(), null, true)
        }
    }

    override fun onDestroyView() {
        rvSearchResults.clearOnScrollListeners()
        adapter.setData(emptyList())
        super.onDestroyView()
    }

    override fun onDetailsClick(userId: String) {
        router.toProfile(userId)
    }

    override fun onSendRequestClick(userId: String) {
        viewModel.sendRequest(userId)
    }

    override fun onCancelRequestClick(requestId: String) {
        viewModel.cancelRequest(requestId)
    }

    override fun onAcceptRequestClick(requestId: String) {
        viewModel.handleRequest(requestId, true)
    }

    override fun onDeclineRequestClick(requestId: String) {
        viewModel.handleRequest(requestId, false)
    }

    override fun onLocateClick(userId: String) {
        viewModel.locate(userId)
    }
}