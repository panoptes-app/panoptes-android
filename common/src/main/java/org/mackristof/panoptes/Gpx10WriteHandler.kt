package org.mackristof.panoptes

import android.util.Log
import org.mackristof.panoptes.Utils.Companion.addToMediaDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile


class Gpx10WriteHandler: Runnable {

    companion object {
        var lock: Object = Object()
    }

    lateinit var dateTimeString: String
    lateinit var loc: Location
    private lateinit var gpxFile: File
    private var addNewTrackSegment: Boolean = false
    val INITIAL_XML_LENGTH = 343

    constructor(dateTimeString: String, gpxFile: File, loc: Location, addNewTrackSegment: Boolean) {
        this.dateTimeString = dateTimeString
        this.addNewTrackSegment = addNewTrackSegment
        this.gpxFile = gpxFile
        this.loc = loc
    }

    override fun run() {
        synchronized(Gpx10WriteHandler.lock) {

            try {
                if (!gpxFile.exists()) {
                    gpxFile.createNewFile()

                    val initialWriter = FileOutputStream(gpxFile, true)
                    val initialOutput = BufferedOutputStream(initialWriter)

                    initialOutput.write(getBeginningXml(dateTimeString).toByteArray())
                    initialOutput.write("<trk>".toByteArray())
                    initialOutput.write(getEndXml().toByteArray())
                    initialOutput.flush()
                    initialOutput.close()

                    //New file, so new segment.
                    addNewTrackSegment = true
                }

                val offsetFromEnd = if (addNewTrackSegment) getEndXml().length else getEndXmlWithSegment().length
                val startPosition = gpxFile.length() - offsetFromEnd
                val trackPoint = getTrackPointXml(loc, dateTimeString)

                val raf = RandomAccessFile(gpxFile, "rw")
                raf.seek(startPosition)
                raf.write(trackPoint.toByteArray())
                raf.close()
                addToMediaDatabase( gpxFile, "text/plain")
                Log.i(Constants.TAG,"Finished writing to GPX10 file")

            } catch (e: Exception) {
                Log.e(Constants.TAG, "Gpx10FileLogger.write", e)
            }

        }

    }

    fun getBeginningXml(dateTimeString: String): String {
        val initialXml = StringBuilder()
        initialXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
        initialXml.append("<gpx version=\"1.0\" creator=\"PANOPTES\" ")
        initialXml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
        initialXml.append("xmlns=\"http://www.topografix.com/GPX/1/0\" ")
        initialXml.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 ")
        initialXml.append("http://www.topografix.com/GPX/1/0/gpx.xsd\">")
        initialXml.append("<time>").append(dateTimeString).append("</time>")
        return initialXml.toString()
    }

    fun getEndXml(): String {
        return "</trk></gpx>"
    }

    fun getEndXmlWithSegment(): String {
        return "</trkseg></trk></gpx>"
    }

    fun getTrackPointXml(loc: Location, dateTimeString: String): String {

        val track = StringBuilder()

        if (addNewTrackSegment) {
            track.append("<trkseg>")
        }

        track.append("<trkpt lat=\"${loc.lat}\" lon=\"${loc.lon}\">")
        track.append("<ele>${loc.corAlt}</ele>")
        track.append("<time>${dateTimeString}</time>")
        track.append("</trkpt>\n")

        track.append("</trkseg></trk></gpx>")

        return track.toString()
    }


}