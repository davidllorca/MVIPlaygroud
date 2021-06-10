package com.mango.mviplayground

import android.app.Application
import timber.log.Timber

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 27/05/2021
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty("kotlinx.coroutines.debug", "on")
        AppFactory.context = this
        Timber.plant(MyTree())
    }
}


private class MyTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val threadName = Thread.currentThread().name
        super.log(priority, tag, "[$threadName] $message", t)
    }
}
