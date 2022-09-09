package com.exa.nanashopper.staging

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exa.nanashopper.staging.databinding.ActivityMainBinding
import com.roam.sdk.Roam
import com.roam.sdk.RoamPublish
import com.roam.sdk.RoamTrackingMode
import com.roam.sdk.callback.*
import com.roam.sdk.models.RoamError
import com.roam.sdk.models.RoamTrip
import com.roam.sdk.models.RoamUser
import com.roam.sdk.models.createtrip.RoamCreateTrip
import org.json.JSONObject
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var roamUserId: String
    private lateinit var roamTripId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Allow Mock location for Development
        Roam.allowMockLocation(true)
        roamTripId = "631999dbbac5e062427516c9"

        binding.btnCreateUser.setOnClickListener { createRoamUser() }
        binding.btnGetUser.setOnClickListener { getUser() }
        binding.btnStartTracking.setOnClickListener { startRoamTracking() }
        binding.btnStopTracking.setOnClickListener { stopRoamTracking() }

        binding.btnCreateTrip.setOnClickListener { createAroundHomeTripe() }
        binding.btnStartTrip.setOnClickListener { startTrip() }
        binding.btnPauseTrip.setOnClickListener { pauseTripXX() }
        binding.btnResumeTrip.setOnClickListener { resumeTripXX() }
        binding.btnGetTrips.setOnClickListener { getTrips() }
        binding.btnEndTrip.setOnClickListener { endTrip() }


    }


    /** Note: Before creating an online trip, we must have to call Roam.createUser() to get a new user ID
     * or if we have already an existing user ID, then we have to call Roam.getUser().
     * */

    // TODO: Step 3 : Create User
    private fun createRoamUser() {

        Roam.createUser("Ahmed", null, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {

                Log.e("TAG", "onSuccess: " + roamUser.userId)
                //Timber.tag("TAG").e(roamUser.userId,"OnSuccess: ")
                roamUserId = roamUser.userId   //initialized user id for later use in other method
                toggleEvents()
            }

            override fun onFailure(error: RoamError) {
                Log.e("TAG", "onFailure: " + error.message)
                //  Timber.tag("TAG").e(error.message, "onFailure: ")
            }
        })
    }

    // TODO: Step 3 : Get User
    private fun getUser() {


        if (TextUtils.isEmpty(binding.editTextUserId.text)) {
            Toast.makeText(this, "Enter user id", Toast.LENGTH_SHORT).show()
            return
        }

        roamUserId = binding.editTextUserId.text.toString()


        Roam.getUser(roamUserId, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {

                roamUserId = roamUser.userId  //initialized user id for later use in other method

                toggleEvents()

                Timber.d("RoamTrackLocation, getUser, onSuccess() enter")
                Timber.d("RoamTrackLocation, getUser, userId> , [${roamUser.userId}]")
                Timber.d("RoamTrackLocation, getUser, description > , [${roamUser.description}]")
                Timber.d("RoamTrackLocation, getUser, geofenceEvents > , [${roamUser.geofenceEvents}]")
                Timber.d("RoamTrackLocation, getUser, tripsEvents > , [${roamUser.tripsEvents}]")
                Timber.d("RoamTrackLocation, getUser, movingGeofenceEvents > , [${roamUser.movingGeofenceEvents}]")
                Timber.d("RoamTrackLocation, getUser, eventListenerStatus > , [${roamUser.eventListenerStatus}]")
                Timber.d("RoamTrackLocation, getUser, locationListenerStatus > , [${roamUser.locationListenerStatus}]")
            }

            override fun onFailure(roamError: RoamError) {

                Timber.d("RoamTrackLocation, getUser, RoamError.code > [${roamError.code}]")
                Timber.d("RoamTrackLocation, getUser, RoamError.message > [${roamError.message}]")

            }
        })
    }


    /**   syncTrip not required for online trip */

//    private fun syncTripXX() {
//        Roam.syncTrip(roamTripId, object : RoamSyncTripCallback {
//            override fun onSuccess(msg: String) {
//                Timber.d("Roam.resumeTrip, syncTripXX() enter")
//
//            }
//
//            override fun onFailure(roamError: RoamError) {
//
//                Timber.d("Roam.resumeTrip, pauseTrip.code [${roamError.code}]")
//                Timber.d("Roam.resumeTrip, pauseTrip.message [${roamError.message}]")
//
//            }
//        })
//
//    }


    // TODO: Step 4 : Toggle events
    private fun toggleEvents() {
        Roam.toggleEvents(true, true, true, true, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                toggleListener()
                Timber.d("RoamTrackLocation, toggleEvents, onSuccess() enter")
            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("RoamTrackLocation, toggleEvents, RoamError.code > [${roamError.code}]")
                Timber.d("RoamTrackLocation, toggleEvents, RoamError.message > [${roamError.message}]")
            }
        })
    }

    // TODO: Step 5 : Toggle listener
    private fun toggleListener() {

        Roam.toggleListener(true, true, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Timber.d("RoamTrackLocation, toggleListener, onSuccess() enter")
                subscribeLocationAndEvents()
                publishSaveLocation()
                callForegroundNotification()
                checkPermissions()
            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("RoamTrackLocation, toggleListener, RoamError.code > [${roamError.code}]")
                Timber.d("RoamTrackLocation, toggleListener, RoamError.message > [${roamError.message}]")
            }
        })
    }

    // TODO: Step 6 : Subscribe to your userId to listen location and events update in LocationReceiver.java
    private fun subscribeLocationAndEvents() {
        Roam.subscribe(
            Roam.Subscribe.BOTH,  //In case of subscribe location and events both
            roamUserId
        )
    }

    // TODO: Step 7 : Publish and save location in Roam Backend.
    private fun publishSaveLocation() {
        val geoSparkPublish = RoamPublish.Builder()
            .build()
        Roam.publishAndSave(geoSparkPublish)
    }

    // TODO: Step 8: Set foreground notification.
    private fun callForegroundNotification() {
        Roam.setForegroundNotification(
            true,
            "Shopper Roam",
            "Go To Roam demo activity",
            R.drawable.ic_launcher_foreground,
            "com.exa.nanashopper.features.splash.view.SplashActivity",
            "Ahmed Text"  // This field must be a path of your app service class which help to run app in background
        )
    }

    // TODO: Step 9 : Grant permission.
    private fun checkPermissions(): Boolean {
        return if (!Roam.checkLocationServices()) {
            Roam.requestLocationServices(this)
            false
        } else if (!Roam.checkLocationPermission()) {
            Roam.requestLocationPermission(this)
            false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Roam.checkBackgroundLocationPermission()) {
            Roam.requestBackgroundLocationPermission(this)
            false
        } else {
            true
        }
    }

    // TODO: Step 10 : Start Tracking.
    private fun startRoamTracking() {

        //Before starting tracking, must have to check all required location permissions

        if (!checkPermissions()) {
            Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show()
            return
        }
        val timeBasedTracking: RoamTrackingMode = RoamTrackingMode.Builder(10)
            .setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH)
            .build()
        Roam.startTracking(timeBasedTracking)


//        val distanceBasedTrackingMode: RoamTrackingMode = RoamTrackingMode.Builder(10,0)
//            .setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH)
//            .build()
//        Roam.startTracking(distanceBasedTrackingMode)

        // Roam.startTracking(RoamTrackingMode.ACTIVE);
        // Roam.startTracking(RoamTrackingMode.BALANCED);
        // Roam.startTracking(RoamTrackingMode.PASSIVE);

    }

    // TODO: Step 11 : Stop Tracking.
    private fun stopRoamTracking() {
        Roam.stopTracking()
    }

    // TODO: Step 12 : Create Trip.
    private fun createAroundHomeTripe() {

        if (!checkPermissions()) {
            Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show()
            return
        }

        val metadata = JSONObject()
        metadata.put("TripName", "TripNumber004")

        Log.e("TAG", "createAroundHomeTripe: " + "called")

        Roam.createTrip(null, null, false, metadata, object : RoamCreateTripCallback {
            override fun onSuccess(roamTrip: RoamCreateTrip) {

                roamTripId = roamTrip.id

//                Timber.d("Roam.createTrip, roamTrip.id [${roamTrip.id}]")
//                Timber.d("Roam.createTrip, roamTrip.user_id [${roamTrip.user_id}]")
//                Timber.d("Roam.createTrip, roamTrip.created_at [${roamTrip.created_at}]")
//                Timber.d("Roam.createTrip, roamTrip.is_started [${roamTrip.is_started}]")
//                Timber.d("Roam.createTrip, roamTrip.is_paused [${roamTrip.is_paused}]")
//                Timber.d("Roam.createTrip, roamTrip.is_ended [${roamTrip.is_ended}]")
//                Timber.d("Roam.createTrip, roamTrip.is_deleted [${roamTrip.is_deleted}]")
//                Timber.d("Roam.createTrip, roamTrip.updated_at [${roamTrip.updated_at}]")
//                Timber.d("Roam.createTrip, roamTrip.trip_tracking_url [${roamTrip.trip_tracking_url}]")
//                Timber.d("Roam.createTrip, roamTrip.origins [${roamTrip.origins}]")
//                Timber.d("Roam.createTrip, roamTrip.destinations [${roamTrip.destinations}]")
                //updateRoamTripId(roamTrip.id)
                Log.e("TAG", "createTripSuccess: " + roamTrip.user_id)
            }

            override fun onFailure(roamError: RoamError) {
                // Timber.d("Roam.createTrip, roamError.code [${roamError.code}]")
                // Timber.d("Roam.createTrip, roamError.message [${roamError.message}]")
                Log.e("TAG", "createTripError: " + roamError.message)
            }
        })
    }


    /** Before start a trip, if we do not call start tracking method then it will default start the tracking
    with Active tracking mode */

    private fun startTrip() {


        if (!checkPermissions()) {
            Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(roamTripId)) {
            Toast.makeText(this, "Trip id error", Toast.LENGTH_SHORT).show()
            return
        }

        Roam.startTrip(roamTripId, "First Trip", object : RoamTripCallback {
            override fun onSuccess(message: String) {
                Timber.d("Roam.startTrip, message [$message]")

            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("Roam.startTrip, roamError.code [${roamError.code}]")
                Timber.d("Roam.startTrip, roamError.message [${roamError.message}]")
            }
        })
    }

    private fun pauseTripXX() {
        if (TextUtils.isEmpty(roamTripId)) {
            Toast.makeText(this, "Trip id error", Toast.LENGTH_SHORT).show()
            return
        }
        Roam.pauseTrip(roamTripId, object : RoamTripCallback {
            override fun onSuccess(message: String) {
                Timber.d("Roam.pauseTrip, message [$message]")

            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("Roam.pauseTrip, pauseTrip.code [${roamError.code}]")
                Timber.d("Roam.pauseTrip, pauseTrip.message [${roamError.message}]")
            }
        })
    }

    private fun resumeTripXX() {
        if (TextUtils.isEmpty(roamTripId)) {
            Toast.makeText(this, "Trip id error", Toast.LENGTH_SHORT).show()
            return
        }
        Roam.resumeTrip(roamTripId, object : RoamTripCallback {
            override fun onSuccess(message: String) {
                Timber.d("Roam.resumeTrip, message [$message]")

            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("Roam.resumeTrip, pauseTrip.code [${roamError.code}]")
                Timber.d("Roam.resumeTrip, pauseTrip.message [${roamError.message}]")
            }
        })
    }

    private fun endTrip() {
        if (TextUtils.isEmpty(roamTripId)) {
            Toast.makeText(this, "Trip id error", Toast.LENGTH_SHORT).show()
            return
        }
        Roam.stopTrip(roamTripId, object : RoamTripCallback {
            override fun onSuccess(message: String) {
                Timber.d("Roam.stopTrip, message [${message}]")

            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("Roam.stopTrip, roamError.message [${roamError.message}]")
                Timber.d("Roam.stopTrip, roamError.code [${roamError.code}]")
            }
        })
    }

    private fun getTrips() {
        Roam.activeTrips(false, object : RoamActiveTripsCallback {
            override fun onSuccess(roamTrip: RoamTrip) {
                Timber.d("Roam.activeTrips, roamTrip.activeTrips.size [${roamTrip.activeTrips.size}]")
                roamTrip.activeTrips.forEach { activeTrip ->
                    binding.editTextTextPersonName.setText(activeTrip.tripId)
                    Timber.d("Roam.activeTrips, activeTrip.tripId [${activeTrip.tripId}]")
                    Timber.d("Roam.activeTrips, activeTrip.createdAt [${activeTrip.createdAt}]")
                    Timber.d("Roam.activeTrips, activeTrip.updatedAt [${activeTrip.updatedAt}]")
                    Timber.d("Roam.activeTrips, activeTrip.isStarted [${activeTrip.isStarted}]")
                    Timber.d("Roam.activeTrips, activeTrip.isPaused [${activeTrip.isPaused}]")
                    Timber.d("Roam.activeTrips, activeTrip.ended [${activeTrip.ended}]")
                    Timber.d("Roam.activeTrips, activeTrip.syncStatus [${activeTrip.syncStatus}]")
                    Timber.d("Roam.activeTrips, activeTrip.deleted [${activeTrip.deleted}]")
                    Timber.d("Roam.activeTrips, activeTrip [${activeTrip.toString()}]")

                }

            }

            override fun onFailure(roamError: RoamError) {
                Timber.d("Roam.activeTrips, roamError.code [${roamError.code}]")
                Timber.d("Roam.activeTrips, roamError.message [${roamError.message}]")
            }
        })
    }

}