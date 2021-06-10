package com.mango.mviplayground.selectcountry.presentation.ui

import androidx.compose.runtime.Immutable
import com.mango.mviplayground.presentation.ToolbarState
import com.mango.mviplayground.selectcountry.data.Country
import com.mango.mviplayground.selectcountry.presentation.CountryViewMapper


class CountryScreenState(
    val bodyState: SelectCountryState,
    val toolbarState: ToolbarState
) {
    override fun toString(): String {
        return "CountryScreenState(bodyState=$bodyState, toolbarState=$toolbarState)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CountryScreenState) return false

        if (bodyState != other.bodyState) return false
        if (toolbarState != other.toolbarState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bodyState.hashCode()
        result = 31 * result + toolbarState.hashCode()
        return result
    }
}

@Immutable
sealed class SelectCountryState(val loading: Boolean) {
    data class Error(val msg: String) : SelectCountryState(false)
    class HappyPath(loading: Boolean, val payload: SelectCountryStatePayload): SelectCountryState(loading) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HappyPath) return false

            if (loading != other.loading) return false
            if (payload != other.payload) return false

            return true
        }

        override fun hashCode(): Int {
            var result = loading.hashCode()
            result = 31 * result + payload.hashCode()
            return result
        }

        override fun toString(): String {
            return "HappyPath($loading, countries=${payload.countryList.size}, displayCountries=${payload.displayCountryList.size}, query=${payload.queryText})"
        }
    }

    companion object {
        val INITIAL = HappyPath(false, SelectCountryStatePayload())
    }
}

data class SelectCountryStatePayload(
    val countryList: List<Country> = emptyList(),
    val displayCountryList: List<CountryView> = emptyList(),
    val queryText: String? = null
)


data class CountryView(val id: CountryKey, val name: String, val language: String)
data class CountryKey(val countryId: String, val languageIso: String)

fun defaultMapper(): CountryViewMapper = { country, selectedLangIso ->
    val key = CountryKey(country.mangoCode, selectedLangIso)
    CountryView(
        key,
        country.name,
        country.languages.first { it.isoCode == selectedLangIso }.displayName
    )
}
