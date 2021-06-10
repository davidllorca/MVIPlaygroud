package com.mango.mviplayground.selectcountry.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.mango.mviplayground.*
import com.mango.mviplayground.databinding.ActivitySelectCountryBinding
import com.mango.mviplayground.presentation.ToolbarState
import com.mango.mviplayground.presentation.toolbarStateReducer
import com.mango.mviplayground.selectcountry.domain.v1.SelectCountryScope
import com.mango.mviplayground.selectcountry.domain.v1.SelectCountryScopeImpl
import com.mango.mviplayground.selectcountry.domain.v1.fetchCountriesUseCase
import com.mango.mviplayground.selectcountry.presentation.ui.*
import com.mango.mviplayground.selectcountry.presentation.viewmodel.CountryListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.reduxkotlin.*
import timber.log.Timber
import kotlin.reflect.jvm.reflect

typealias SuspendThunk<State> = suspend (dispatch: Dispatcher, getState: GetState<State>, extraArg: Any?) -> Any

fun <State> createCoroutineThunkMiddleWare(parentScope: CoroutineScope, backgroundDispatcher: CoroutineDispatcher, extraArgument: Any? = null): Middleware<State> = middleware { store, next, action ->
    if (action is Function<*>) {
        val kfun = requireNotNull(action.reflect())
        try {
            if (kfun.isSuspend) {
                val suspendThunk = (action as SuspendThunk<*>)
                parentScope.launch(CoroutineName("suspend-thunk")) {
                    try {
                        suspendThunk(store.dispatch, store.getState, extraArgument)
                    } catch (e: Throwable) {
                        if (e is CancellationException) {
                            throw e
                        } else {
                            Timber.e(e, "Failure in suspend thunk")
                        }
                    }
                }
            } else {
                @Suppress("UNCHECKED_CAST")
                val thunk = (action as Thunk<*>)
                parentScope.launch(CoroutineName("bg-thunk")) {
                    try {
                        withContext(backgroundDispatcher) {
                            thunk(store.dispatch, store.getState, extraArgument)
                        }
                    } catch (e: Throwable) {
                        if (e is CancellationException) {
                            throw e
                        } else {
                            Timber.e(e, "Failure in thunk")
                        }
                    }
                }
            }
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Dispatching functions must use type Thunk, or SuspendThunk", e)
        }

    } else {
        next(action)
    }
}

class SelectCountryActivity : AppCompatActivity() {

    private val analyticTracker: AnalyticTracker by lazy { AppFactory.getTracker() }
    private val navigator: Navigator by lazy { AppFactory.getNavigator() }

    // RFC: Injected maybe?
    private val reducer by lazy { SelectCountryReducer() }

    // Boilerplate, extract common initializations
    private val store by lazy {
        createThreadSafeStore(
            reducer::countryStateReducer,
            SelectCountryState.INITIAL,
            applyMiddleware(
                createThunkMiddleware(),
                createLoggingMiddleware()
            )
        )
    }

    // TODO: use DI instead
    private val selectCountryScope: SelectCountryScope by lazy { SelectCountryScopeImpl(lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides this) {
                ProvideDispatch(store.dispatch, selectCountryScope) {
                    val state by produceState(store.state) {
                        val subscriptor = store.subscribe {
                            Timber.d("State updated to: ${store.state}")
                            value = store.state
                        }
                        awaitDispose(subscriptor)
                    }
                    CountryScreen(state)
                }
            }
        }

        // execute firs load
        store.dispatch(selectCountryScope.fetchCountriesUseCase())
    }
}

class TraditionalSelectCountryActivity : AppCompatActivity() {
    // I would put analytic logic in specific middleware (given that we have actions, and state transitions available there it should be possible)
    private val analyticTracker: AnalyticTracker by lazy { AppFactory.getTracker() }


    // I would model navigations as `SideEffects`
    private val navigator: Navigator by lazy { AppFactory.getNavigator() }


    private val viewModel: CountryListViewModel by viewModels(ChupyFakeFactoryProvider())

    private var binding: ActivitySelectCountryBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCountryBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            viewModel.renderer.connect(this, it)
        }


        lifecycleScope.launchWhenStarted {

            // We may find useful to create also side effect handling middleware (as store can dispatch Any kind of action)
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    is SideEffect.ToastEffect -> Toast.makeText(this@TraditionalSelectCountryActivity, effect.msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}


// all this should be DI

private fun screenReducer(bodyReducer: (SelectCountryState, Any) -> SelectCountryState): (CountryScreenState, Any) -> CountryScreenState = { state, action ->
    CountryScreenState(
        bodyReducer(state.bodyState, action),
        toolbarStateReducer(state.toolbarState, action)
    )
}

class CountryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        check(modelClass == CountryListViewModel::class.java) { "This factory is single class factory!!" }

        val factory = CoroutineStoreFactory { scope ->
            // RFC: Injected maybe?
            val reducer = SelectCountryReducer()

            // Boilerplate, extract common initializations
            createThreadSafeStore(
                screenReducer(reducer::countryStateReducer),
                CountryScreenState(SelectCountryState.INITIAL, ToolbarState.Title("Selecciona un país")),
                applyMiddleware(
                    createCoroutineThunkMiddleWare(scope, Dispatchers.IO),
                    createLoggingMiddleware()
                )
            )
        }

        return CountryListViewModel(factory) as T
    }
}

class ChupyFakeFactoryProvider : () -> ViewModelProvider.Factory {
    override fun invoke(): ViewModelProvider.Factory {
        return CountryViewModelFactory()
    }
}
