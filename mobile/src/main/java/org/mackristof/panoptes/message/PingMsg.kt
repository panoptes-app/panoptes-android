package org.mackristof.panoptes.message

import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.NodeApi
import com.google.android.gms.wearable.Wearable
import org.mackristof.panoptes.Constants
import org.mackristof.panoptes.MainActivity
import kotlin.concurrent.thread


class PingMsg constructor(context: Context, text: String?, callback: (nodeWearId:String) -> Unit): Msg, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    override val path = Constants.COMMAND_PING
    override val text: String? = text
    private var mApiClient: GoogleApiClient? = null
    var callback = callback


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
        if (messageEvent.path == Constants.COMMAND_PING) {
            //TODO display wellconnected
            callback(messageEvent.sourceNodeId)
//            MainActivity.getInstance().nodeWearId = messageEvent.sourceNodeId
            MainActivity.getInstance().mStatusText?.text = "wearable connected to "+ messageEvent.sourceNodeId
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
            var nodes: NodeApi.GetConnectedNodesResult = Wearable.NodeApi.getConnectedNodes(mApiClient).await()

            for (node in nodes.getNodes()) {
                Wearable.MessageApi.sendMessage(
                        mApiClient, node.getId(), path, text?.toByteArray()).await();

            }
        }
    }
}