package hu.sztomek.wheresmybuddy.presentation.common

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife
import timber.log.Timber

abstract class BaseActivity<M: Parcelable, VM: BaseViewModel<M>> : AppCompatActivity() {

    protected lateinit var viewModel: VM

    companion object {
        const val KEY_STATE = "ui_model_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("${javaClass.simpleName}: onCreate [$savedInstanceState]")

        setContentView(getLayoutId())
        ButterKnife.bind(this)

        viewModel = ViewModelProviders.of(this, getViewModelFactory()).get(getViewModelClass())
        viewModel.state.observe(this, Observer {
            state ->
                Timber.d("${javaClass.simpleName}: onChange[$state]")
                handleStateChange(state)
        })

        restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Timber.d("${javaClass.simpleName}: onSaveInstanceState [$outState]")
        saveState(outState)
    }

    fun saveState(outBundle: Bundle?) {
        outBundle?.putParcelable(KEY_STATE, getPersistableUiState())
    }


    fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState?.containsKey(KEY_STATE) == true) {
            viewModel.setModel(savedInstanceState.getParcelable<M>(KEY_STATE))
        }
    }

    abstract fun getLayoutId(): Int
    abstract fun getPersistableUiState(): M
    abstract fun getViewModelFactory(): ViewModelProvider.Factory
    abstract fun getViewModelClass(): Class<VM>
    abstract fun handleStateChange(state: State<M>?)

}