package com.mango.mviplayground.selectcountry.data

import android.content.Context
import android.util.Log
import arrow.core.Either
import com.mango.mviplayground.AppFactory
import com.mango.mviplayground.selectcountry.domain.CountriesRepo

class FakeCountriesRepoImpl(private val context: Context) : CountriesRepo {

    override suspend fun getCountries(): Either<List<Country>, Error> {
        val parser = AppFactory.getParser()

        return context.assets.open("countries_response.json").use {
            Either.left(parser.readValue(it, CountryResponse::class.java).countries)
        }
    }

    override suspend fun setCountry(country: Country): Either<Unit, Error> {
        Log.d("CountryRepo", "${country.analyticsLabel} set as country")
        return Either.left(Unit)
    }

    override suspend fun getCountry(code: String): Either<Country, Error> {
        return when (val result = getCountries()) {
            is Either.Left -> Either.left(result.a.first { it.mangoCode == code })
            is Either.Right -> Either.right(Error())
        }
    }
}
