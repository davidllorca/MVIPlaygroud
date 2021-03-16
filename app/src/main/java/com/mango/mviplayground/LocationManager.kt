package com.mango.mviplayground

import android.util.Log

interface LocationManager {
    fun setUserLocale(languageCode: String, regionCode: String)
}

class FakeLocationManger : LocationManager {
    override fun setUserLocale(languageCode: String, regionCode: String) {
        Log.d("LocationManager", "Local set with values [ lang=$languageCode, region=$regionCode ]")
    }

}