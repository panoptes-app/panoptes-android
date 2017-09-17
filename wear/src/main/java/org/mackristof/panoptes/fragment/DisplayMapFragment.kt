package org.mackristof.panoptes.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.mackristof.panoptes.R



class DisplayMapFragment:  Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    var mMap: GoogleMap? = null
    val SYDNEY = LatLng(-33.85704, 151.21522)


    override fun  onCreateView( inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState:  Bundle?): View? {
        val view = inflater?.inflate(R.layout.map_fragment, container, false)!!
        val mMapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mMapFragment.getMapAsync(this);

        return view
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.setOnMapLongClickListener(this);
        mMap?.addMarker(MarkerOptions().position(SYDNEY)
                .title("Sydney Opera House"));
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 10.toFloat()));
    }

    override fun onMapLongClick(p0: LatLng?) {
        throw UnsupportedOperationException()
    }
}