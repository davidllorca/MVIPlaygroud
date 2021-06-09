package com.mango.mviplayground.selectcountry

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.mango.mviplayground.*
import com.mango.mviplayground.selectcountry.domain.SelectCountryScope
import com.mango.mviplayground.selectcountry.domain.SelectCountryScopeImpl
import com.mango.mviplayground.selectcountry.domain.fetchCountriesUseCase
import com.mango.mviplayground.selectcountry.ui.CountryScreen
import com.mango.mviplayground.selectcountry.ui.SelectCountryReducer
import com.mango.mviplayground.selectcountry.ui.SelectCountryState
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createThreadSafeStore
import org.reduxkotlin.createThunkMiddleware

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
                            Log.d("Borrame", "State updated to: ${store.state}")
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
