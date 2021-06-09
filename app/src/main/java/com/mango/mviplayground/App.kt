package com.mango.mviplayground

import android.app.Application

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 27/05/2021
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppFactory.context = this
    }
}
