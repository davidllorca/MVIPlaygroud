package com.mango.mviplayground.selectcountry.domain.v2

import arrow.core.Either
import com.mango.mviplayground.AppFactory
import com.mango.mviplayground.LocationManager
import com.mango.mviplayground.selectcountry.presentation.SuspendThunk
import com.mango.mviplayground.selectcountry.domain.CountriesRepo
import com.mango.mviplayground.selectcountry.presentation.CountryAction
import com.mango.mviplayground.selectcountry.presentation.ui.CountryScreenState
import com.mango.mviplayground.selectcountry.presentation.ui.SelectCountryState
import kotlinx.coroutines.*
import org.reduxkotlin.Dispatcher
import timber.log.Timber

interface SelectCountryDomain {
    val countryRepo: CountriesRepo
    val locationManager: LocationManager
}

class SelectCountryDomainImpl() :SelectCountryDomain {
    override val countryRepo: CountriesRepo by lazy { AppFactory.getCountryRepo() }
    override val locationManager: LocationManager by lazy { AppFactory.getLocationManager() }
}

fun SelectCountryDomain.fetchCountriesUseCase(): SuspendThunk<CountryScreenState> = { dispatch: Dispatcher, getState: () -> CountryScreenState, extraArg: Any? ->
    Timber.d("Will load countries")
    dispatch(CountryAction.Loading)
    when (val countries = countryRepo.getCountries()) {
        is Either.Left -> dispatch(CountryAction.SetCountries(countries.a))
        is Either.Right -> dispatch(CountryAction.SetError("Failed to get countries: ${countries.b.message}"))
    }
}

fun SelectCountryDomain.filterCountriesUseCase(filterText: String?): SuspendThunk<CountryScreenState> = thunk@{ dispatch, getState, _ ->
    val state = getState()

    val originalState = state.bodyState as? SelectCountryState.HappyPath ?: return@thunk state
    if (originalState.payload.queryText == filterText) {
        return@thunk state
    }
    dispatch(CountryAction.Loading)
    dispatch(CountryAction.SetFilterText(filterText))
    val countries = originalState.payload.countryList
    if (filterText.isNullOrBlank()) {
        dispatch(CountryAction.SetCountries(countries))
    } else {
        val filteredItems = countries.filter {
            it.name.contains(
                filterText,
                ignoreCase = true
            )
        }
        dispatch(CountryAction.SetCountries(countries, filteredItems))
    }
}
