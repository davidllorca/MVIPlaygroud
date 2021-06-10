package com.mango.mviplayground.selectcountry.presentation.viewmodel

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.mango.mviplayground.BindingRenderer
import com.mango.mviplayground.databinding.ActivitySelectCountryBinding
import com.mango.mviplayground.databinding.ItemCountryBinding
import com.mango.mviplayground.selectcountry.presentation.ui.CountryScreenState
import com.mango.mviplayground.selectcountry.presentation.ui.CountryView
import com.mango.mviplayground.selectcountry.presentation.ui.SelectCountryState
import com.mango.mviplayground.presentation.ToolbarState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.properties.Delegates

class CountryListRenderer(private val interactions: CountryInteractions) :
    BindingRenderer<CountryScreenState, ActivitySelectCountryBinding> {

    // configure state to only handle latest, if we're not getting states cause we're not started just ignore them
    private val currentState =
        MutableSharedFlow<CountryScreenState>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override fun connect(owner: LifecycleOwner, binding: ActivitySelectCountryBinding) {
        owner.lifecycleScope.launch {
            var watcher: TextWatcher? = null
            try {
                watcher = binding.groupToolbar.inputToolbarSearch.doAfterTextChanged {
                    interactions.textUpdated(it?.toString())
                }

                binding.groupToolbar.icToolbarSearch.setOnClickListener {
                    interactions.searchRequested()
                }

                binding.groupToolbar.icToolbarClearSearch.setOnClickListener {
                    interactions.onClearSearch()
                }

                currentState
                    .flowWithLifecycle(owner.lifecycle)
                    .collect { actualRender(binding, it) }
            } finally {
                watcher?.let { binding.groupToolbar.inputToolbarSearch.removeTextChangedListener(it) }
            }
        }
    }

    private fun actualRender(target: ActivitySelectCountryBinding, state: CountryScreenState) {
        val bodyState = state.bodyState
        target.loadingGroup.visibility = if (bodyState.loading) View.VISIBLE else View.GONE

        when (bodyState) {
            is SelectCountryState.Error -> {
                target.listCountries.visibility = View.GONE
                target.layoutError.root.visibility = View.VISIBLE
                target.layoutError.tvMsg.text = bodyState.msg
            }
            is SelectCountryState.HappyPath -> {
                target.listCountries.visibility = View.VISIBLE
                target.layoutError.root.visibility = View.GONE
                val adapter: Adapter = target.listCountries.adapter as? Adapter ?: Adapter().also {
                    target.listCountries.adapter = it
                }
                adapter.items = bodyState.payload.displayCountryList
            }
        }

        when (val toolbarState = state.toolbarState) {
            is ToolbarState.Search -> {
                target.groupToolbar.layoutToolbarSearchMode.visibility = View.VISIBLE
                target.groupToolbar.layoutToolbarTitleMode.visibility = View.GONE
                if (target.groupToolbar.inputToolbarSearch.text?.toString() != toolbarState.query) {
                    target.groupToolbar.inputToolbarSearch.setText(toolbarState.query)
                }
            }
            is ToolbarState.Title -> {
                target.groupToolbar.layoutToolbarSearchMode.visibility = View.GONE
                target.groupToolbar.layoutToolbarTitleMode.visibility = View.VISIBLE
                target.groupToolbar.tvToolbarTitle.text = toolbarState.title
            }
        }
    }

    override fun renderState(state: CountryScreenState) {
        if (!currentState.tryEmit(state)) {
            Timber.w("Dropping UI state updates, nobody listening??")
        }
    }

    private inner class Adapter : RecyclerView.Adapter<ItemCountryViewHolder>() {
        var items: List<CountryView> by Delegates.observable(emptyList()) { _, initial, final ->
            Timber.tag("BORRAME").d("Should notify?")
            if (initial !== final) {
                Timber.tag("BORRAME").d("Yessss!!")
                notifyDataSetChanged()
            } else {
                Timber.tag("BORRAME").d("No way!!")
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemCountryViewHolder {
            return ItemCountryViewHolder(
                ItemCountryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), interactions::selectedCountry)
        }

        override fun onBindViewHolder(holder: ItemCountryViewHolder, position: Int) {
            holder.setCountry(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }
}
