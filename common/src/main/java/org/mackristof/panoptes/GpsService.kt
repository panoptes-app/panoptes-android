package org.mackristof.panoptes

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.common.api.ResultCallback
//import com.google.android.gms.common.api.Status
import android.location.LocationListener
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable


class GpsService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    var mGoogleApiClient: GoogleApiClient? = null
    var broadcaster: LocalBroadcastManager? = null
    var currentLocation: org.mackristof.panoptes.Location? = null
    var geoGrid: GeoGrid?=null
    var mlocManager: LocationManager?= null
    var gpsSatsAvailable: Int = 0
    private var gpsLocationListerner: GpsLocationListener? = null



    override fun onCreate(){
        Log.i(Constants.TAG,"gpsService created")
    }

    override fun onDestroy(){
        Log.i(Constants.TAG,"gpsService stopped ")
        if (Utils.isRunningOnWatch(this) &&  mGoogleApiClient != null && (mGoogleApiClient as GoogleApiClient).isConnected ){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, gpsLocationListerner)
            (mGoogleApiClient as GoogleApiClient).disconnect()
        } else {
            mlocManager?.removeUpdates(gpsLocationListerner)
        }

    }

    override fun onStartCommand(intent: Intent? , flags: Int, startId: Int ):Int {
        Log.i(Constants.TAG,"gpsService started ")
        broadcaster = LocalBroadcastManager.getInstance(applicationContext)
        if ( Utils.hasGPS(this.applicationContext) && Utils.isGpsEnabled(this.applicationContext) ) {
            geoGrid = GeoGrid(this)
            gpsLocationListerner = GpsLocationListener()
            if (Utils.isRunningOnWatch(this)){
                mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                        .addApiIfAvailable(LocationServices.API)
                        .addApiIfAvailable(Wearable.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build()
                mGoogleApiClient?.connect()
            } else {
                mlocManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mlocManager?.addGpsStatusListener(gpsLocationListerner)
                mlocManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, gpsLocationListerner)
            }
        } else {
            //TODO send message to start gps on other side
        }

        return super.onStartCommand(intent, flags, startId)
    }




    override fun onConnected(p0: Bundle?) {
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "connected, wait for location ..."))
        if (Utils.isRunningOnWatch(this)) {
            val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(Constants.GPS_UPDATE_INTERVAL_MS)
                    .setFastestInterval(Constants.GPS_FASTEST_INTERVAL_MS)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, gpsLocationListerner)
                    .setResultCallback(ResultCallback<Status> { status ->
                        if (status.status.isSuccess) {
                            if (Log.isLoggable(Constants.TAG, Log.DEBUG)) {
                                Log.d(Constants.TAG, "Successfully requested location updates")
                            }
                        } else {
                            Log.e(Constants.TAG,
                                    "Failed in requesting location updates, status code: ${status.statusCode}, message: ${status.statusMessage} ")
                        }
                    })

        }

    }





    private inner class GpsLocationListener: LocationListener, GpsStatus.Listener, com.google.android.gms.location.LocationListener {

        override fun onLocationChanged(location: android.location.Location) {

            val currentLocation = org.mackristof.panoptes.Location(location.time,
                    location.latitude,
                    location.longitude,
                    if (location.hasAltitude() ) location.altitude else Double.NaN,
                    if (location.hasAltitude() ) (location.altitude - geoGrid?.GetAltitudeCorrection(location.latitude, location.longitude)!!) else Double.NaN,
                    location.accuracy,
                    gpsSatsAvailable)



            broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "located").putExtra(Constants.INTENT_LOCATION, currentLocation))
            Log.i(Constants.TAG, "location changed: (${location.latitude}, ${location.longitude} / atl : ${location.altitude}) with acc ${location.accuracy} on ${location.provider}")


        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//            when (status) {
//                LocationProvider.OUT_OF_SERVICE -> broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "GPS Out of Service"))
//                LocationProvider.TEMPORARILY_UNAVAILABLE -> broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "GPS Temporarily Unavailable"))
//                LocationProvider.AVAILABLE -> {
//                }
//            }
        }

        override fun onGpsStatusChanged(event: Int) {
            when (event) {
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> getNbSats()
            }
        }

        override fun onProviderDisabled(provider: String?) {
            Log.i(Constants.TAG, "gps disabled !!!")
        }

        override fun onProviderEnabled(provider: String?) {
            Log.i(Constants.TAG, "gps enabled !!!")
        }


        fun getNbSats() {
            val it = mlocManager?.getGpsStatus(null)?.getSatellites()?.iterator()
            if (it == null) {
                gpsSatsAvailable = 0;
            } else {
                var i: Int = 0;
                while (it.hasNext()) {
                    it.next()
                    i += 1
                }
                gpsSatsAvailable = i
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(Constants.TAG, "onConnectionSuspended(): connection to location client suspended")
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this as com.google.android.gms.location.LocationListener )
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection suspended"))
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(Constants.TAG, "onConnectionFailed(): " + connectionResult?.errorMessage)
        broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "gps connection failed"))
    }


}



