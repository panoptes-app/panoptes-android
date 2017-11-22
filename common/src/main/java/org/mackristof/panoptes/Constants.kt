package org.mackristof.panoptes

class Constants {
    companion object {
        val COMMAND_ACTIVATE_GPS = "/startGPS"
        val COMMAND_DESACTIVATE_GPS = "/stopGPS"
        val COMMAND_PING = "/ping"
        val COMMAND_START = "/start"
        val COMMAND_STOP = "/stop"
        val COMMAND_IS_GPS = "/gps"
        val CONNECTION_TIME_OUT_MS: Long = 100
        val INTENT_LOCATION = "location"
        val INTENT_LOCATION_EXTRA_PUBLISH = "publish"
        val INTENT_LOCATION_STATUS = "status"
        val TAG = javaClass.`package`.name
        val GPS_UPDATE_INTERVAL_MS: Long = 1000
        val GPS_FASTEST_INTERVAL_MS: Long = 0
        val DATA_ITEM_PATH_LOCATION = "/location"
        val INTENT_MQTT_URL = "url"
        val INTENT_MQTT_LOGIN = "login"
        val INTENT_MQTT_PASSWORD = "password"
        val INTENT_START_TRACK_LOCATION = "start_track"


    }
}