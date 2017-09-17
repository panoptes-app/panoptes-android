package org.mackristof.panoptes.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.mackristof.panoptes.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import org.mackristof.panoptes.Constants
import org.mackristof.panoptes.Utils


class LocationPublisherService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var broadcaster: LocalBroadcastManager? = null


    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        LocationPublisherServiceInstance = this
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(Constants.TAG,"locationPublisher started ")
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
        return super.onStartCommand(intent, flags, startId)
    }



    override fun onConnected(p0: Bundle?) {
        broadcaster = LocalBroadcastManager.getInstance(this)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException()
        //TODO what can do on this event ??
    }





    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_LOCATION)){
                if (Utils.isRunningOnWatch(context!!)) {
                    LocationPublisherService.getInstance().putLocationToDataApi(intent.getParcelableExtra(Constants.INTENT_LOCATION))
                } else {
                    LocationPublisherService.getInstance().publishCurrentLocation(intent.getParcelableExtra(Constants.INTENT_LOCATION))
                }
            }
        }

    }


    fun publishCurrentLocation(location: Location){
        Log.i(Constants.TAG, "go to publish last location at "+ location.timestamp)
    }


    fun putLocationToDataApi( location: Location) {
        val putDataMapReq = PutDataMapRequest.create(Constants.DATA_ITEM_PATH_LOCATION)
        putDataMapReq.dataMap.putDouble("lat", location.lat)
        putDataMapReq.dataMap.putLong("time", location.timestamp)
        putDataMapReq.dataMap.putDouble("lon", location.lon)
        putDataMapReq.dataMap.putDouble("alt", location.alt)
        putDataMapReq.dataMap.putFloat("acc", location.acc)
        val putDataRequest = putDataMapReq.asPutDataRequest()
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest)
    }


    companion object {
        private var LocationPublisherServiceInstance: LocationPublisherService? = null
        fun getInstance(): LocationPublisherService {
            if (LocationPublisherServiceInstance !=null) {
                return LocationPublisherServiceInstance as LocationPublisherService
            } else {
                throw IllegalStateException("LocationPublisherService instance is null")
            }
        }
    }

}