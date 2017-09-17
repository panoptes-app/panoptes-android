package org.mackristof.panoptes.message

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi


interface Msg: GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    val path: String
    val text: String?
}