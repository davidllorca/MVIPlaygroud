package com.mango.mviplayground

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mango.mviplayground.selectcountry.data.FakeCountriesRepoImpl
import com.mango.mviplayground.selectcountry.domain.CountriesRepo

object AppFactory {

    lateinit var context: Context

    fun getCountryRepo(): CountriesRepo = FakeCountriesRepoImpl(context)

    fun getLocationManager(): LocationManager = FakeLocationManger()

    fun getTracker(): AnalyticTracker = FakeTracker()

    fun getNavigator(): Navigator = Navigator()

    fun getParser(): ObjectMapper {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.disable(
            MapperFeature.AUTO_DETECT_GETTERS,
            MapperFeature.AUTO_DETECT_IS_GETTERS,
            MapperFeature.AUTO_DETECT_SETTERS
        )
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)

        return mapper
    }
}
