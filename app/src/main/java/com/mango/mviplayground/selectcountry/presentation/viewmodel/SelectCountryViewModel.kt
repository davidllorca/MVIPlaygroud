package com.mango.mviplayground.selectcountry.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.mango.mviplayground.BindingRenderer
import com.mango.mviplayground.CoroutineStoreFactory
import com.mango.mviplayground.SideEffect
import com.mango.mviplayground.StateViewModel
import com.mango.mviplayground.databinding.ActivitySelectCountryBinding
import com.mango.mviplayground.databinding.ItemCountryBinding
import com.mango.mviplayground.presentation.ToolbarAction
import com.mango.mviplayground.selectcountry.domain.v2.SelectCountryDomain
import com.mango.mviplayground.selectcountry.domain.v2.SelectCountryDomainImpl
import com.mango.mviplayground.selectcountry.domain.v2.fetchCountriesUseCase
import com.mango.mviplayground.selectcountry.domain.v2.filterCountriesUseCase
import com.mango.mviplayground.selectcountry.presentation.ui.*
import kotlinx.coroutines.launch

interface CountryInteractions {
    fun selectedCountry(countryId: CountryKey)
    fun textUpdated(text: String?)
    fun searchRequested()
    fun onClearSearch()
}

class ItemCountryViewHolder(
    val binding: ItemCountryBinding,
    private val onCountrySelected: (CountryKey) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private var current: CountryView? = null
    init {
        binding.root.setOnClickListener {
            current?.let { country ->
                onCountrySelected(country.id)
            }
        }
    }
    fun setCountry(country: CountryView) {
        current = country
        binding.apply {
            tvName.text = country.name
            tvLanguage.text = country.language
        }
    }
}

class CountryListViewModel(factory: CoroutineStoreFactory<CountryScreenState>) : StateViewModel<CountryScreenState, BindingRenderer<CountryScreenState, ActivitySelectCountryBinding>>(factory) {

    // also should be DI
    private val domain: SelectCountryDomain = SelectCountryDomainImpl()

    private val interactions: CountryInteractions = object : CountryInteractions {
        override fun selectedCountry(key: CountryKey) {
            check(currentState.bodyState is SelectCountryState.HappyPath) { "Select country called while in invalid state!!" }
            val state = currentState.bodyState as SelectCountryState.HappyPath
            val country = requireNotNull(state.payload.countryList.find { it.mangoCode == key.countryId })
            val lang = country.languages.first { it.isoCode == key.languageIso }
            viewModelScope.launch {
                dispatchSideEffect(SideEffect.ToastEffect("Selected country: ${country.name} - ${lang.displayName}"))
            }
        }

        override fun textUpdated(text: String?) {
            dispatchMessage(ToolbarAction.SetQuery(text))
            dispatchMessage(domain.filterCountriesUseCase(text))
        }

        override fun searchRequested() {
            dispatchMessage(ToolbarAction.SetQuery(""))
        }

        override fun onClearSearch() {
            dispatchMessage(ToolbarAction.SetTitle("Selecciona un país"))
            dispatchMessage(domain.filterCountriesUseCase(null))
        }
    }
    override fun createRenderer(): BindingRenderer<CountryScreenState, ActivitySelectCountryBinding> {
        return CountryListRenderer(interactions)
    }

    override fun onFinishInitialize() {
        dispatchMessage(domain.fetchCountriesUseCase())
    }
}
