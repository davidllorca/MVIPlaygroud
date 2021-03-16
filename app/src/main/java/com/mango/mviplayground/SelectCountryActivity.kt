package com.mango.mviplayground

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Either
import com.google.android.material.textfield.TextInputEditText


class SelectCountryActivity : AppCompatActivity(), CountriesAdapter.OnClickCountry {

    private lateinit var toolbarTitleView: View
    private lateinit var toolbarSearchView: View
    private lateinit var inputText: TextInputEditText
    private lateinit var recycler: RecyclerView
    private lateinit var noResultsTv: View
    private lateinit var errorView: View
    private lateinit var retryButton: Button

    private val countryRepo: CountriesRepo by lazy { AppFactory.getCountryRepo() }
    private val locationManager: LocationManager by lazy { AppFactory.getLocationManager() }
    private val analyticTracker: AnalyticTracker by lazy { AppFactory.getTracker() }
    private val navigator: Navigator by lazy { AppFactory.getNavigator() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_country)

        recycler = findViewById(R.id.list_countries)
        with(recycler) {
            adapter = CountriesAdapter(this@SelectCountryActivity)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        toolbarTitleView = findViewById<View>(R.id.layout_toolbar_title_mode).apply {
            findViewById<ImageButton>(R.id.ic_toolbar_search).apply {
                setOnClickListener { onSearchIconClicked() }
            }
        }
        toolbarSearchView = findViewById<View>(R.id.layout_toolbar_search_mode).apply {
            findViewById<ImageButton>(R.id.ic_toolbar_clear_search).apply {
                setOnClickListener { onClearInputClicked() }
            }
            inputText = findViewById<TextInputEditText>(R.id.input_toolbar_search).apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // Not implemented yet
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        filterCountries(s)
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // Not implemented yet
                    }

                })
            }
        }

        noResultsTv = findViewById(R.id.tv_no_results)
        errorView = findViewById(R.id.layout_error)
        retryButton = findViewById(R.id.bt_error_view_retry)
    }

    override fun onStart() {
        super.onStart()
        loadCountries()
    }

    private fun loadCountries() {
        when (val countries = countryRepo.getCountries()) {
            is Either.Left -> {
                (recycler.adapter as CountriesAdapter).updateCountries(countries.a.toCountryView())
                noResultsTv.visibility = if (countries.a.isEmpty()) View.VISIBLE else View.GONE
                errorView.visibility = View.GONE
            }
            is Either.Right -> errorView.visibility = View.VISIBLE
        }
    }

    private fun filterCountries(input: CharSequence) {
        when (val countries = countryRepo.getCountries()) {
            is Either.Left -> {
                val filter = countries.a.filter {
                    it.name.contains(
                        input,
                        ignoreCase = true
                    )
                }
                (recycler.adapter as CountriesAdapter).updateCountries(filter.toCountryView())
                noResultsTv.visibility = if (filter.isEmpty()) View.VISIBLE else View.GONE
                errorView.visibility = View.GONE
            }
            is Either.Right -> errorView.visibility = View.VISIBLE
        }
    }

    private fun onSearchIconClicked() {
        toolbarTitleView.visibility = View.GONE
        toolbarSearchView.visibility = View.VISIBLE
    }

    private fun onClearInputClicked() {
        toolbarTitleView.visibility = View.VISIBLE
        toolbarSearchView.visibility = View.GONE

        inputText.text?.clear()

        loadCountries()
    }

    override fun onClickCountryItem(item: CountryView) {
        when (val result = countryRepo.getCountry(item.code)) {
            is Either.Left -> {
                setLocationConfig(result.a)

                sendSelectCountryEvent(result.a)

                // TODO("Implement navigation to Home screen")
                // navigator.???????
            }
            is Either.Right -> { // TODO("Implement whatever you want)
            }
        }
    }

    private fun sendSelectCountryEvent(country: Country) {
        analyticTracker.sendEvent(
            mapOf(
                "category" to "user-config",
                "action" to "select-country",
                "label" to country.analyticsLabel
            )
        )
    }

    private fun setLocationConfig(country: Country) {
        locationManager.setUserLocale(
            country.languages.first().isoCode,
            country.isoCode
        )
    }

    private fun List<Country>.toCountryView(): List<CountryView> =
        map { CountryView(it.mangoCode, it.name, it.languages.first().label) }
}

class CountriesAdapter(
    private val listener: OnClickCountry
) : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

    private var values: List<CountryView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.name
        holder.contentView.text = item.languageLabel
        holder.item.setOnClickListener { listener.onClickCountryItem(item) }
    }

    override fun getItemCount(): Int = values.size

    fun updateCountries(countries: List<CountryView>) {
        values = countries
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item: View = view.findViewById(R.id.layout_item_country)
        val idView: TextView = view.findViewById(R.id.tv_name)
        val contentView: TextView = view.findViewById(R.id.tv_language)
    }

    interface OnClickCountry {
        fun onClickCountryItem(item: CountryView)
    }
}

class CountryView(val code: String, val name: String, val languageLabel: String)