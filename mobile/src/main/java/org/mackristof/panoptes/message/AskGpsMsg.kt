package org.mackristof.panoptes.message

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.mackristof.panoptes.Constants
import kotlin.concurrent.thread


class AskGpsMsg constructor(context: Context, nodeId: String, text: String?, succes: (nodeWearId:String) -> Unit, failure:() -> Unit): Msg, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    override val path = Constants.COMMAND_IS_GPS
    override val text: String? = text
    private var mApiClient: GoogleApiClient? = null
    val nodeId = nodeId
    val succes = succes
    val failure = failure


    init {
        if (mApiClient == null) {
            mApiClient = GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addApi(Wearable.API)
                    .build()
            mApiClient?.connect()
        }

    }

    // on receiving msg from wearable device
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == Constants.COMMAND_IS_GPS) {
            //TODO if not gps on watch ativate on smartphone
            Log.i(Constants.TAG,"gps activated on swatch ? "+String(messageEvent.data))
            if (String(messageEvent.data)=="true"){
                succes(messageEvent.sourceNodeId)
            } else {
                failure()
            }
            mApiClient?.disconnect()
        }

    }

    //on google wearable connected
    override fun onConnected(bundle: Bundle?) {
        Wearable.MessageApi.addListener(mApiClient,this)
    }

    //on google wearable suspended
    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException()
    }
    fun sendMessage() {
        thread() {
            Log.i(Constants.TAG, "send GpsMsg")
            Wearable.MessageApi.sendMessage(mApiClient, nodeId, path, text?.toByteArray()).await();

        }
    }
}