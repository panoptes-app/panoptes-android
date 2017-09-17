package org.mackristof.panoptes

import android.content.Context
import android.util.Log

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.util.concurrent.Semaphore

import kotlin.android.system.services.R

/**
 * Created by mackristof on 30/06/2016.
 */
class GeoGrid(internal val context: Context) {

    var IsAvailable = false
    var IsLoadingFromFile = false
    internal var LoadingGridValues = Array(GRID_SIZE) { ShortArray(1440) }
    private val SemaphoreParms = Semaphore(1, false)
    internal var GridValues = Array(GRID_SIZE) { ShortArray(1440) }
    internal var StartLatitude: Double = 0.toDouble()

    internal inner class LoadEGM96Grid(private val centerToLatitude: Double, private val context: Context) : Runnable {

        override fun run() {
            val inputStream = context.resources.openRawResource(R.raw.ww15mgh)
            val bin = BufferedInputStream(inputStream)
            val din = DataInputStream(bin)
            var i: Int
            var ilon: Int
            var ilat = 0

            var istartlat: Int
            istartlat = ((90.0 - centerToLatitude) / 0.25f - (GRID_SIZE / 2 - 1)).toInt()
            if (istartlat < 0) {
                istartlat = 0
            }
            if (istartlat > 721 - GRID_SIZE) {
                istartlat = 721 - GRID_SIZE
            }
            val count = (2076480 / 2 / 1440).toInt()
            i = 0
            while (i < count && ilat < GRID_SIZE) {
                try {
                    ilon = 0
                    while (ilon < 1440) {
                        LoadingGridValues[ilat][ilon] = din.readShort()
                        ilon++
                    }
                    if (i >= istartlat) ilat++
                } catch (e: IOException) {
                    Log.e(Constants.TAG, "error while reading EGM96 Grid", e)
                }

                i++
            }
            SemaphoreParms.acquireUninterruptibly()
            StartLatitude = 90.0 - istartlat.toDouble() * 0.25f
            for (ii in GridValues.indices)
                for (jj in 0..GridValues[ii].size - 1)
                    GridValues[ii][jj] = LoadingGridValues[ii][jj]
            SemaphoreParms.release()
            IsAvailable = true
            IsLoadingFromFile = false
        }
    }

    private fun Load(centerToLatitude: Double) {
        Thread(LoadEGM96Grid(centerToLatitude, this.context)).start()
    }

    fun GetAltitudeCorrection(latitude: Double, longitude: Double): Double {
        if (IsAvailable) {
            SemaphoreParms.acquireUninterruptibly()
            val Lat = 90.0 - latitude
            var Lon = longitude
            if (Lon < 0) {
                Lon += 360.0
            }
            var ilon = (Lon / 0.25).toInt()
            val ilat = ((Lat - (-StartLatitude + 90.0)) / 0.25).toInt()
            if (ilat < 0 || ilat > GRID_SIZE - 2) {
                IsAvailable = false
                IsLoadingFromFile = true
                SemaphoreParms.release()
                Load(latitude)
                return 0.toDouble()
            }
            val istartlat: Int
            istartlat = ((90.0 - latitude) / 0.25f - (GRID_SIZE / 2 - 1)).toInt()
            val hc11 = GridValues[ilat][ilon]
            val hc12 = GridValues[ilat + 1][ilon]
            ilon++
            if (ilon > 1439) {
                ilon -= 1440
            }
            val hc21 = GridValues[ilat][ilon]
            val hc22 = GridValues[ilat + 1][ilon]

            SemaphoreParms.release()

            val hc1 = hc11 + (hc12 - hc11) * (Lat % 0.25) / 0.25
            val hc2 = hc21 + (hc22 - hc21) * (Lat % 0.25) / 0.25

            val hc = hc1 + (hc2 - hc1) * (Lon % 0.25) / 0.25


            if (ilat < 1 && istartlat > 0 || ilat > GRID_SIZE - 3 && istartlat < 721 - GRID_SIZE) {
                if (!IsLoadingFromFile) {
                    IsLoadingFromFile = true
                    Load(latitude)
                }
            }
            return hc / 100
        } else {
            if (!IsLoadingFromFile) {
                IsLoadingFromFile = true
                Load(latitude)
            }
            return 0.toDouble()
        }
    }

    companion object {

        val GRID_SIZE = 8  // This value MUST Be even;
    }
}