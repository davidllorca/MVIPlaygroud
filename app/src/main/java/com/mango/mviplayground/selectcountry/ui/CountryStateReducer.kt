package com.mango.mviplayground.selectcountry.ui

import com.mango.mviplayground.selectcountry.data.Country

sealed class CountryAction {
    object Loading : CountryAction()
    class SetFilterText(val queryText: String? = null) : CountryAction()
    class SetCountries(val countries: List<Country>, val displayed: List<Country> = countries) : CountryAction()
    class SetError(val msg: String) : CountryAction()
}

fun countryStateReducer(state: SelectCountryState, action: Any): SelectCountryState {
    val countryAction = action as? CountryAction ?: return state
    return when (countryAction) {
        CountryAction.Loading -> when (state) {
            is SelectCountryState.Error -> SelectCountryState.HappyPath(
                true,
                SelectCountryStatePayload()
            )
            is SelectCountryState.HappyPath -> SelectCountryState.HappyPath(true, state.payload)
        }
        is CountryAction.SetCountries -> if (state is SelectCountryState.HappyPath) {
            SelectCountryState.HappyPath(
                false,
                SelectCountryStatePayload(
                    countryList = countryAction.countries,
                    displayCountryList = countryAction.displayed,
                    queryText = state.payload.queryText
                )
            )
        } else {
            // Nothing in error state
            state
        }
        is CountryAction.SetFilterText -> if (state is SelectCountryState.HappyPath) {
            SelectCountryState.HappyPath(
                state.loading,
                state.payload.copy(queryText = countryAction.queryText)
            )
        } else {
            // Nothing in error state
            state
        }
        is CountryAction.SetError -> SelectCountryState.Error(countryAction.msg)
    }
}
