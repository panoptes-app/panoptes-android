package org.mackristof.panoptes.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.data.FreezableUtils
import com.google.android.gms.wearable.*
import org.mackristof.panoptes.Constants
import org.mackristof.panoptes.Location



class MobileWearableListenerService : WearableListenerService(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {



    private var mGoogleApiClient: GoogleApiClient? = null
    private var nodeConnected = false

    var broadcaster: LocalBroadcastManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(Constants.TAG,"MobileWearableListenerService started ")
        broadcaster = LocalBroadcastManager.getInstance(applicationContext)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
        super.onCreate()
    }

    override fun onConnected(p0: Bundle?) {
        nodeConnected = true
    }
    override fun onConnectionSuspended(p0: Int) {
        nodeConnected = false
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        nodeConnected = false
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        for (event in FreezableUtils.freezeIterable(dataEvents)) {
            val putDataMapRequest = PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.dataItem))
            if (event.type == DataEvent.TYPE_CHANGED && Constants.DATA_ITEM_PATH_LOCATION.equals(event.dataItem.uri.path)) {
                val data = putDataMapRequest.dataMap
                val location = Location(
                        timestamp = data.getLong("time"),
                        lat = data.getDouble("lat"),
                        lon = data.getDouble("lon"),
                        alt = data.getDouble("alt"),
                        corAlt = Double.NaN,
                        acc = data.getFloat("acc"),
                        nbSats = 0
                )
                Log.i(Constants.TAG, "receiving location at ${location.timestamp} with ${location.lat}/${location.lon}")
                broadcaster?.sendBroadcast(Intent(Constants.INTENT_LOCATION).putExtra(Constants.INTENT_LOCATION_STATUS, "located").putExtra(Constants.INTENT_LOCATION, location))
            }
        }

    }
}