package org.mackristof.panoptes

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GpxLoggerService: Service() {

    private lateinit var timestamp: String

    private lateinit var gpxFile: File

    private lateinit var broadcaster: LocalBroadcastManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        gpxLoggerService = this
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        broadcaster = LocalBroadcastManager.getInstance(this)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
        return super.onStartCommand(intent, flags, startId)
    }

    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_LOCATION)){
                GpxLoggerService.getInstance().writeCurrentLocation(intent.getParcelableExtra(Constants.INTENT_LOCATION))
            }
        }

    }

    private class StartTrackBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_START_TRACK_LOCATION)){
                GpxLoggerService.getInstance().startTrack(intent.getParcelableExtra(Constants.INTENT_START_TRACK_LOCATION))
            }
        }

    }

    private fun startTrack(firstLocation: Location) {
        timestamp = formatDate(firstLocation.timestamp)
        gpxFile = File("${timestamp}.gpx")
    }

    private fun formatDate(timestamp: Long): String {
        var sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm")
        return sdf.format(Date(timestamp))
    }

    private fun writeCurrentLocation(location: Location){
        val dateTimeString = formatDate(location.timestamp)
        val writeHandler = Gpx10WriteHandler(dateTimeString, gpxFile, location, false)
    }

    companion object {
        private var gpxLoggerService: GpxLoggerService? = null
        fun getInstance(): GpxLoggerService {
            if (GpxLoggerService !=null) {
                return GpxLoggerService as GpxLoggerService
            } else {
                throw IllegalStateException("GpxLoggerService instance is null")
            }
        }
    }

}
