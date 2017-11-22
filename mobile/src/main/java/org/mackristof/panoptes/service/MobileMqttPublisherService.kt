package org.mackristof.panoptes.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.mackristof.panoptes.Constants
import org.mackristof.panoptes.Location
import java.text.SimpleDateFormat
import java.util.*


class MobileMqttPublisherService: Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MqttCallbackExtended {



    private var mGoogleApiClient: GoogleApiClient? = null
    private var broadcaster: LocalBroadcastManager? = null
    private var mqttServerUri: String? = null
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private var login: String? = null
    private var password: String? = null
    private var connected: Boolean = false



    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        MobileMqttPublisherServiceInstance = this
        super.onCreate()
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mqttServerUri = intent?.extras?.get(Constants.INTENT_MQTT_URL) as String?
        login = intent?.extras?.get(Constants.INTENT_MQTT_LOGIN) as String?
        password=  intent?.extras?.get(Constants.INTENT_MQTT_PASSWORD) as String?

        Log.i(Constants.TAG,"MobileMqttPublisherService started for url ${mqttServerUri}")
        connectWearAPI()

        if (mqttServerUri != null) connectToBroker()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun connectWearAPI() {
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
    }

    private fun connectToBroker() {
        mqttAndroidClient = MqttAndroidClient(applicationContext, mqttServerUri, MqttClient.generateClientId())
        mqttAndroidClient.setCallback(this)
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = login
        mqttConnectOptions.password = password?.toCharArray()
        val actionListener = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i(Constants.TAG, "connected to broker ")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e(Constants.TAG, "could not connect to broker cause : ${exception?.message}")
            }

        }
        mqttAndroidClient.connect(mqttConnectOptions, null, actionListener)
    }

    //mqtt connection completed
    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        if (reconnect) {
            Log.i(Constants.TAG,"Connection lost. Reconnected to : ${serverURI}")
            connectToBroker()
        } else {
            Log.i(Constants.TAG, "Connected to: ${serverURI}")
            connected = true
        }
    }

    // mqtt connection lost
    override fun connectionLost(cause: Throwable?) {
        Log.e(Constants.TAG,"Deconnected from : ${mqttServerUri} ", cause)
        connectToBroker()
    }

    // mqtt delivery complete
    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    // mqtt msg arrived
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Log.i(Constants.TAG, "msg arrived from topic ${topic} with msg : ${message}")
    }

    // Wear API connected
    override fun onConnected(p0: Bundle?) {
        broadcaster = LocalBroadcastManager.getInstance(this)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
    }

    // Wear API connection Suspended
    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException()
        //TODO what can do on this event ??
    }





    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_LOCATION)){
                MobileMqttPublisherService.getInstance().publishCurrentLocation(intent.getParcelableExtra(Constants.INTENT_LOCATION))
            }
        }

    }


    fun publishCurrentLocation(location: Location){
        if (connected) {
            Log.i(Constants.TAG, "go to publish last location at ${SimpleDateFormat("HH:mm:ss").format(Date(location.timestamp))}")
            mqttAndroidClient.publish("/client/1", location.toJsonObject().toString(0).toByteArray(Charsets.UTF_8), 1, false)
        } else {
            Log.w(Constants.TAG, "cannot publish last location at ${SimpleDateFormat("HH:mm:ss").format(Date(location.timestamp))} cause not connected")
        }

    }

    companion object {
        private var MobileMqttPublisherServiceInstance: MobileMqttPublisherService? = null
        fun getInstance(): MobileMqttPublisherService {
            if (MobileMqttPublisherServiceInstance !=null) {
                return MobileMqttPublisherServiceInstance as MobileMqttPublisherService
            } else {
                throw IllegalStateException("MobileMqttPublisherService instance is null")
            }
        }
    }

}