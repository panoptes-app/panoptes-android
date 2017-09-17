package org.mackristof.panoptes.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import org.mackristof.panoptes.MainWearActivity
import org.mackristof.panoptes.Constants
import org.mackristof.panoptes.GpsService
import org.mackristof.panoptes.Utils
import java.util.concurrent.TimeUnit

class WearMessageListener: WearableListenerService() {

    override fun onCreate(){
        super.onCreate()
        Log.i(MainWearActivity.TAG,"WearableListenerService started")
    }

    override fun onMessageReceived(messageEvent: MessageEvent){
        if (messageEvent.path == Constants.COMMAND_PING) {
            Log.i(MainWearActivity.TAG, messageEvent.path + " (" + String(messageEvent.data) + ")")
            val i = Intent(applicationContext as Context, MainWearActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            reply(Constants.COMMAND_PING, "pong",messageEvent.sourceNodeId)
        } else if (messageEvent.path == Constants.COMMAND_IS_GPS) {
            reply(Constants.COMMAND_IS_GPS, (Utils.hasGPS(applicationContext) && Utils.isGpsEnabled(applicationContext)).toString(), messageEvent.sourceNodeId)
        } else if (messageEvent.path == Constants.COMMAND_ACTIVATE_GPS){
            if (Utils.hasGPS(applicationContext) && Utils.isGpsEnabled(applicationContext)){
                //TODO start Service
                val intentLoc = Intent(this, GpsService::class.java)
                if (!stopService(intentLoc)) {
                    startService(intentLoc)
                }
            }
        } else {
            super.onMessageReceived( messageEvent )
        }
    }

    private fun reply(path: String, message: String?, senderId: String) {
        val client = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .build()
        client.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        Wearable.MessageApi.sendMessage(client, senderId, path, message?.toByteArray());
        client.disconnect();
    }

}