package com.mango.mviplayground.data

import com.mango.mviplayground.CountriesRepo
import com.mango.mviplayground.FakeCountriesRepoImpl
import org.junit.Test

class FakeCountriesRepoImplTest {

    val sut: CountriesRepo = FakeCountriesRepoImpl()

    @Test
    fun `when countries are requested then proper response is returned`() {
        val countries = sut.getCountries()

        println(countries)
    }
}