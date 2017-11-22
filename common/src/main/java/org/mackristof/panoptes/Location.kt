package org.mackristof.panoptes

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject


data class Location (val timestamp: Long,
                     val lat: Double,
                     val lon: Double,
                     val alt: Double,
                     val corAlt: Double,
                     val acc: Float,
                     val nbSats: Int): Parcelable {

    constructor(source: Parcel): this(source.readLong(),source.readDouble(), source.readDouble(), source.readDouble(), source.readDouble(), source.readFloat(), source.readInt())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(this.timestamp)
        dest?.writeDouble(this.lat)
        dest?.writeDouble(this.lon)
        dest?.writeDouble(this.alt)
        dest?.writeDouble(this.corAlt)
        dest?.writeFloat(this.acc)
        dest?.writeInt(this.nbSats)
    }

    fun toJsonObject(): JSONObject  {
        val jsonObject = JSONObject()
        jsonObject.put("timestamp", timestamp)
        jsonObject.put("lat", lat)
        jsonObject.put("lon", lon)
        jsonObject.put("alt", alt)
        jsonObject.put("corAlt", corAlt)
        jsonObject.put("acc", acc)
        jsonObject.put("nbsats", nbSats)
        return jsonObject
    }

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<Location> = object : Parcelable.Creator<Location> {
            override fun createFromParcel(source: Parcel): Location{
                return Location(source)
            }

            override fun newArray(size: Int): Array<Location?> {
                return arrayOfNulls(size)
            }
        }
    }
}