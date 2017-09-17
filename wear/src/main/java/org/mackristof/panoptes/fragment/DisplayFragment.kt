package org.mackristof.panoptes.fragment

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import org.mackristof.panoptes.*
import java.text.SimpleDateFormat



class DisplayFragment : Fragment() {
    private var mStatusView: TextView? = null
    private var mTimeView: TextView? = null
    private var mAccProgressBar: ProgressBar? = null
    private var broadcaster: LocalBroadcastManager? = null


    override fun  onCreateView( inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState:  Bundle?): View? {
        val view = inflater?.inflate(R.layout.display_fragment, container, false)!!
        displayFragmentInstance = this
        mStatusView = view.findViewById(R.id.status) as TextView
        mTimeView = view.findViewById(R.id.time) as TextView
        mAccProgressBar = view.findViewById(R.id.acc) as ProgressBar
        mAccProgressBar?.progress = 0
                broadcaster = LocalBroadcastManager.getInstance(this.context)
        broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
        return view
    }


    override fun onStop(){

        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
        super.onStop()


    }

    override fun onPause(){
        broadcaster?.unregisterReceiver(LocationBroadcastReceiver())
        super.onPause()
    }

    override fun onResume(){
        if (Utils.isServiceRunning(this.context, GpsService::class.java.name)){
            broadcaster?.registerReceiver(LocationBroadcastReceiver(), IntentFilter(Constants.INTENT_LOCATION))
        }
        super.onResume()

    }


    private class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra(Constants.INTENT_LOCATION)){
                val location: Location = intent.getParcelableExtra(Constants.INTENT_LOCATION)
                val displayedLoc =
                        """
location : ${SimpleDateFormat("HH:mm:ss").format(location.timestamp)}
${String.format("%.1f",location.lat)}, ${String.format("%.1f",location.lon)}
atl : ${String.format(".%1f",location.corAlt)}(${String.format("%.1f",location.alt)})
acc ${String.format("%.1f",location.acc)}
nb Sats ${location.nbSats}
"""
                DisplayFragment.getInstance().updateDisplay(location, displayedLoc)
            } else {
                DisplayFragment.getInstance().updateDisplay(null, intent.getStringExtra(Constants.INTENT_LOCATION_STATUS))
            }
        }

    }

    private fun updateDisplay(location: Location?, status: String) {
        if (location !=null) {
            mTimeView!!.text = SimpleDateFormat("HH:mm:ss").format(location?.timestamp)
            if (location != null) {
                mAccProgressBar?.progress = if (location.acc < 10) 100 else if (location.acc < 50) 50 else 20
                val color = if (location.acc < 10) Color.GREEN else if (location.acc < 50) Color.YELLOW else Color.RED
                mAccProgressBar?.setProgressTintList(ColorStateList.valueOf(color))

            }
        }
        mStatusView?.text = status
    }



    companion object {

        private var displayFragmentInstance: DisplayFragment? = null
        fun getInstance(): DisplayFragment {
            if (displayFragmentInstance !=null) {
                return displayFragmentInstance as DisplayFragment
            } else {
                throw IllegalStateException("DisplayFragment instance is null")
            }
        }
    }


}
