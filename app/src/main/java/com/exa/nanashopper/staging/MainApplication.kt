package com.exa.nanashopper.staging

import android.app.Application
import android.util.Log
import com.roam.sdk.Roam

open class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // TODO: Step 1 : Toggle events
        Log.d("Logs", "Enter MainApplication onCreate()")
        Roam.initialize(this, "f9d623f76cce03e6af079fa069d210c163f77ef30d60dda078a46d0265d67430")

    }
}