package org.mackristof.panoptes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable
import org.mackristof.panoptes.message.ActivateGpsMsg
import org.mackristof.panoptes.message.AskGpsMsg
import org.mackristof.panoptes.message.PingMsg
import org.mackristof.panoptes.service.MobileWearableListenerService
import java.text.SimpleDateFormat


class MainActivity: AppCompatActivity(), ConnectionCallbacks, OnConnectionFailedListener {

    var mStatusText: TextView? = null
    var nodeWearId: String? = null
    var mGoogleApiClient:GoogleApiClient? = null
    var wearableNodeId: String? = null
    var broadcaster: LocalBroadcastManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityInstance = this
        Log.i(Constants.TAG,"mainActivity created")
        setContentView(R.layout.activity_main)
        val mButtonStartWear = findViewById(R.id.mButtonStartWear) as Button
        val mButtonStartPhone = findViewById(R.id.mButtonStartPhone) as Button
        mStatusText = findViewById(R.id.statusText) as TextView
        mGoogleApiClient = googleApiClient()
        mGoogleApiClient?.connect()
        mButtonStartWear.setOnClickListener { attemptStartTracking() }
        mButtonStartPhone.setOnClickListener { startLocServiceOnPhone() }

        broadcaster = LocalBroadcastManager.getInstance(this)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
    }

    fun attemptStartTracking() {

        fun connectWearable(){

            fun askGPS(nodeWearId:String) {
                fun gpsActivatedOnWear(nodeWearId: String){
                    Log.i(Constants.TAG,"GPS activated on Wear $nodeWearId")
                }

                fun startLocService(){
                    val intentLoc = Intent(this, GpsService::class.java)
                    if (!stopService(intentLoc)) {
                        startService(intentLoc)
                    }
                }

                fun activateWearGPS(nodeWearId: String){
                    ActivateGpsMsg(applicationContext,nodeWearId,null, ::gpsActivatedOnWear, ::startLocService).sendMessage()
                }

                AskGpsMsg(applicationContext,nodeWearId,null, ::activateWearGPS, ::startLocService ).sendMessage()
            }
            PingMsg(applicationContext,"hello", ::askGPS).sendMessage()

        }
        if (isWearableAPIExist()) {
            connectWearable()
            listenWearLocation()
        } else {
            mStatusText?.text = "wearable node not found "
            startLocServiceOnPhone()
        }
    }

    fun listenWearLocation(){
        val intentMsg = Intent(this, MobileWearableListenerService::class.java)
        if (!Utils.isServiceRunning(applicationContext, MobileWearableListenerService::class.java.name)) {
            startService(intentMsg)
        }
    }

    fun startLocServiceOnPhone(){
        val intentLoc = Intent(this, GpsService::class.java)
        if (!stopService(intentLoc)) {
            startService(intentLoc)
        }
    }

    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_LOCATION)){
                val location: Location = intent.getParcelableExtra(Constants.INTENT_LOCATION)
                val displayedLoc = """
location : ${SimpleDateFormat("HH:mm:ss").format(location.timestamp)}
${String.format("%.1f",location?.lat)}, ${String.format("%.1f",location?.lon)}
atl : ${String.format(".%1f",location?.corAlt)}(${String.format("%.1f",location?.alt)})
acc ${String.format("%.1f",location?.acc)}
nb Sats ${location?.nbSats}
"""
                MainActivity.getInstance().updateDisplay(displayedLoc)
            } else {
                MainActivity.getInstance().updateDisplay(intent.getStringExtra(Constants.INTENT_LOCATION_STATUS))
            }
        }

    }

    private fun updateDisplay(location: String) {
        mStatusText?.text = location
    }


    private fun googleApiClient(): GoogleApiClient? {
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(Wearable.API)
                .addApi(LocationServices.API).build()
    }

    override fun onConnectionSuspended(cause: Int) {
        mStatusText?.text = "connection suspended cause: " + cause
    }

    override fun onConnectionFailed(result: ConnectionResult){
        if (result.errorCode == ConnectionResult.API_UNAVAILABLE) {
            mStatusText?.text = "no wearable api found"
        }
    }

    //google api connected
    override fun onConnected(context: Bundle?) {
        mStatusText?.text = "connected to google play api"
    }

    override fun onStop(){
//        val intentLoc = Intent(this, GpsService::class.java)
//        stopService(intentLoc)
//        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
//        mGoogleApiClient?.disconnect()
        super.onStop()

    }

    override fun onPause(){
        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
        super.onPause()

    }

    override fun onResume(){
        if (Utils.isServiceRunning(this, GpsService::class.java.name)){
            broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
        }
        super.onResume()

    }

    private fun isWearableAPIExist(): Boolean {
        return (mGoogleApiClient!=null && (mGoogleApiClient as GoogleApiClient).hasConnectedApi(Wearable.API))
    }


    companion object{
        private var MainActivityInstance: MainActivity? = null
        fun getInstance(): MainActivity {
            if (MainActivityInstance!=null) {
                return MainActivityInstance as MainActivity
            } else {
                throw IllegalStateException("MainActivity instance is null")
            }
        }
    }

}
