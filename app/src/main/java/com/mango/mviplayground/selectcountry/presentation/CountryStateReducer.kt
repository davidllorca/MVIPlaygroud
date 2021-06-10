package com.mango.mviplayground.selectcountry.presentation

import com.mango.mviplayground.selectcountry.data.Country
import com.mango.mviplayground.selectcountry.presentation.ui.CountryView
import com.mango.mviplayground.selectcountry.presentation.ui.SelectCountryState
import com.mango.mviplayground.selectcountry.presentation.ui.SelectCountryStatePayload
import com.mango.mviplayground.selectcountry.presentation.ui.defaultMapper

sealed class CountryAction {
    object Loading : CountryAction()
    class SetFilterText(val queryText: String? = null) : CountryAction()
    class SetCountries(val countries: List<Country>, val displayed: List<Country> = countries) : CountryAction()
    class SetError(val msg: String) : CountryAction()
}

typealias CountryViewMapper = (Country, String) -> CountryView

/**
 * Reducers, only compute state transitions (valid ones, according input action and previous state)
 *
 * Return updated state if need to
 */
class SelectCountryReducer(
    private val mapper: CountryViewMapper = defaultMapper()
) {
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
                        displayCountryList = countryAction.displayed.flatMap { ctry ->
                            ctry.languages.map {
                                mapper(
                                    ctry,
                                    it.isoCode
                                )
                            }
                        },
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
}
