package com.exa.nanashopper.staging

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.exa.nanashopper.staging.ShareDate.isUpdateTheLanguageBefore
import com.exa.nanashopper.staging.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Check what is the reason prevent  app from go to StartingActivity on Android 12 [S]?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isUpdateTheLanguageBefore) {
            isUpdateTheLanguageBefore = true
            updateLocalLanguage()
            return
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOpenRoamTrackLocation.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }


}