package com.mango.mviplayground

import android.util.Log
import arrow.core.Either

interface CountriesRepo {
    fun getCountries(): Either<List<Country>, Error>
    fun setCountry(country: Country): Either<Unit, Error>
    fun getCountry(code: String): Either<Country, Error>
}

class FakeCountriesRepoImpl : CountriesRepo {

    override fun getCountries(): Either<List<Country>, Error> {
        val parser = AppFactory.getParser()

        val response = javaClass.classLoader?.getResourceAsStream(
            "countries_response.json"
        ).use { parser.readValue(it, CountryResponse::class.java) }

        return Either.left(response.countries)
    }

    override fun setCountry(country: Country): Either<Unit, Error> {
        Log.d("CountryRepo", "${country.analyticsLabel} set as country")
        return Either.left(Unit)
    }

    override fun getCountry(code: String): Either<Country, Error> {
        return when (val result = getCountries()) {
            is Either.Left -> Either.left(result.a.first { it.mangoCode == code })
            is Either.Right -> Either.right(Error())
        }
    }
}
