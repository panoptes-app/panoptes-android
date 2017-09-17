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


class ActivateGpsMsg constructor(context: Context, nodeId: String, text: String?, succes: (nodeWearId:String) -> Unit, failure:() -> Unit): Msg, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    override val path = Constants.COMMAND_ACTIVATE_GPS
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
        if (messageEvent.path == Constants.COMMAND_ACTIVATE_GPS) {
            Log.i(Constants.TAG,"gps locked with "+String(messageEvent.data))
            if (messageEvent.data.toString()=="true"){
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
            Log.i(Constants.TAG, "send ActivateGpsMsg")
            Wearable.MessageApi.sendMessage(mApiClient, nodeId, path, text?.toByteArray()).await();

        }
    }
}