package com.mango.mviplayground

import android.util.Log

interface AnalyticTracker {
    fun sendEvent(payload: Map<String, String>)
}

class FakeTracker : AnalyticTracker {
    override fun sendEvent(payload: Map<String, String>) {
        Log.d("AnalyticTracker", "Send event -> $payload")
    }
}