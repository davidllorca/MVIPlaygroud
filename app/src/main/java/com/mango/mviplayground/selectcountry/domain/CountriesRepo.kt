package com.mango.mviplayground.selectcountry.domain

import arrow.core.Either
import com.mango.mviplayground.selectcountry.data.Country

interface CountriesRepo {
    suspend fun getCountries(): Either<List<Country>, Error>
    suspend fun setCountry(country: Country): Either<Unit, Error>
    suspend fun getCountry(code: String): Either<Country, Error>
}
