package com.mango.mviplayground

import com.fasterxml.jackson.annotation.JsonProperty

data class CountryResponse(
    val countries: List<Country>
)

data class Country(
    @JsonProperty("code") val mangoCode: String,
    val name: String,
    val analyticsLabel: String,
    val isoCode: String,
    @JsonProperty("isoMango") val mangoFakeIsoCode: String,
    val languages: List<Language>,
    val currency: Currency,
    val online: Boolean,
    val cartMaxUnits: Int,
    val cartMaxAmount: Int,
    val freeShippingMinAmount: Double,
    val shipmentPrice: Double,
)

data class CountryCodes(
    @JsonProperty("code") val mangoFakeIsoCode: String,
    @JsonProperty("iso2") val isoCode: String
)

data class Currency(
    val code: String,
    val format: String,
    val symbol: String
)

data class Language(
    @JsonProperty("code") override val mangoCode: String,
    @JsonProperty("iso2") override val isoCode: String,
    val label: String
) : LanguageDescriptor {
    override val displayName: String = label
}

interface LanguageDescriptor {
    val displayName: String
    val isoCode: String
    val mangoCode: String
}