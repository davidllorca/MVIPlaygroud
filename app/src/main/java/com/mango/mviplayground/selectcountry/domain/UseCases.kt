package com.mango.mviplayground.selectcountry.domain

import arrow.core.Either
import com.mango.mviplayground.AppFactory
import com.mango.mviplayground.LocationManager
import com.mango.mviplayground.selectcountry.ui.CountryAction
import com.mango.mviplayground.selectcountry.ui.SelectCountryState
import com.mango.mviplayground.toSafeDispatcher
import kotlinx.coroutines.*
import org.reduxkotlin.Dispatcher
import org.reduxkotlin.Thunk

interface SelectCountryScope : CoroutineScope {
    val countryRepo: CountriesRepo
    val locationManager: LocationManager
    val defaultBackgroundDispatcher: CoroutineDispatcher
}

class SelectCountryScopeImpl(delegateScope: CoroutineScope) : SelectCountryScope, CoroutineScope by delegateScope {
    override val countryRepo: CountriesRepo by lazy { AppFactory.getCountryRepo() }
    override val locationManager: LocationManager by lazy { AppFactory.getLocationManager() }
    override val defaultBackgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
}

fun SelectCountryScope.fetchCountriesUseCase(backgroundDispatcher: CoroutineDispatcher = defaultBackgroundDispatcher): Thunk<SelectCountryState> = { dispatch: Dispatcher, getState: () -> SelectCountryState, extraArg: Any? ->

    val safeDispatch = dispatch.toSafeDispatcher()

    dispatch(CountryAction.Loading)
    launch {
        withContext(backgroundDispatcher) {
            when (val countries = countryRepo.getCountries()) {
                is Either.Left -> safeDispatch(CountryAction.SetCountries(countries.a))
                is Either.Right -> safeDispatch(CountryAction.SetError("Failed to get countries: ${countries.b.message}"))
            }
        }
    }
}

fun SelectCountryScope.filterCountriesUseCase(filterText: String?, backgroundDispatcher: CoroutineDispatcher = defaultBackgroundDispatcher): Thunk<SelectCountryState> = thunk@{ dispatch, getState, _ ->
    val safeDispatcher = dispatch.toSafeDispatcher()
    val state = getState()
    val originalState = state as? SelectCountryState.HappyPath ?: return@thunk state
    if (originalState.payload.queryText == filterText) {
        return@thunk state
    }
    launch {
        safeDispatcher(CountryAction.Loading)
        safeDispatcher(CountryAction.SetFilterText(filterText))
        val countries = originalState.payload.countryList
        if (filterText.isNullOrBlank()) {
            safeDispatcher(CountryAction.SetCountries(countries))
        } else {
            val filteredItems = countries.filter {
                it.name.contains(
                    filterText,
                    ignoreCase = true
                )
            }
            safeDispatcher(CountryAction.SetCountries(countries, filteredItems))
        }
    }
}
