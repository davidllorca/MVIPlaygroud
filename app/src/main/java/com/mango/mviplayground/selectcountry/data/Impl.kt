package com.mango.mviplayground.selectcountry.data

import android.util.Log
import arrow.core.Either
import com.mango.mviplayground.AppFactory
import com.mango.mviplayground.selectcountry.domain.CountriesRepo

class FakeCountriesRepoImpl : CountriesRepo {

    override suspend fun getCountries(): Either<List<Country>, Error> {
        val parser = AppFactory.getParser()

        val response = javaClass.classLoader?.getResourceAsStream(
            "countries_response.json"
        ).use { parser.readValue(it, CountryResponse::class.java) }

        return Either.left(response.countries)
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
