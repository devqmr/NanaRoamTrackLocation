package com.exa.nanashopper.staging

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.roam.sdk.Roam
import com.zeugmasolutions.localehelper.LocaleHelper
import com.zeugmasolutions.localehelper.LocaleHelperApplicationDelegate
import timber.log.Timber

class BaseApplication : MainApplication() {


    private val localeAppDelegate = LocaleHelperApplicationDelegate()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localeAppDelegate.attachBaseContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeAppDelegate.onConfigurationChanged(this)
    }

    override fun getApplicationContext(): Context =
        LocaleHelper.onAttach(super.getApplicationContext())


    override fun onCreate() {
        super.onCreate()

        Log.d("Logs", "Enter BaseApplication onCreate()")


        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return super.createStackElementTag(element) + ':' + element.lineNumber
            }
        })


    }
}