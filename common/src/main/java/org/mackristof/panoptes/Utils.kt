package org.mackristof.panoptes

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat

class Utils {

    companion object {
        fun isRunningOnWatch(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
        }

        fun hasGPS(context: Context): Boolean {
            return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
        }

        fun isGpsEnabled(context: Context):Boolean{
           return  (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun isServiceRunning(context: Context, serviceName: String?): Boolean{
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val services = activityManager.getRunningServices(Integer.MAX_VALUE)
            return (services.filter { runningServiceInfo -> runningServiceInfo.service.className == serviceName }.size) > 0
        }

        fun generateNotification(context: Context, icon: Int, activityClass: Class<Activity>,  msg: String){
            val notifIntent = Intent(context, activityClass)
            notifIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val intent = PendingIntent.getActivity(context, 0, notifIntent, 0)
            val notification = NotificationCompat.Builder(context)
                    .setSmallIcon(icon)
                    .setContentIntent(intent)
                    .setContentText(msg)
                    .setPriority(5)
                    .setAutoCancel(true)

                    .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0,notification)

        }
    }


}

