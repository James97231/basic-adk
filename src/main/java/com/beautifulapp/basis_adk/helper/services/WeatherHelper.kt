package com.beautifulapp.basis_adk.helper.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.MediaType
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread

object WeatherHelper {
    private val TAG = "WeatherHelper"
    private val REFRESH_STEP: Long = 600000
    private val APP_ID = "f06216e1f1ad61a002c6c5d5edd44708"
    private val APP_ID_ROBIN = "2aaf363403060b4bc1dce00e333337d4"
    private val LAST_UPDATE_KEY = "my_meteo_timestamp"
    private val NBPREVISIONDAY = 1
    private val UNIT = "metric"
    private val LANGUAGE = "fr"

    /**
     * Interrogation de API pour la recuperation d'informations sur la metéo.
     */
    fun findWeather(latitude: Double, longitude: Double, handler: WeatherDataHandler) {
        val url =
            "http://api.openweathermap.org/data/2.5/forecast/daily?APPID=$APP_ID_ROBIN&lat=$latitude&lon=$longitude&units=$UNIT&cnt=$NBPREVISIONDAY&lang=$LANGUAGE"
        Log.e(TAG, "url: $url")
        HttpRequestHelper.get(
            url,
            listener = object :
                HttpRequestHelper.HttpRequestListener {
                override fun onCompleted(
                    isSuccessful: Boolean,
                    content: ByteArray?,
                    contentType: MediaType?,
                    exception: IOException?
                ) {
                    exception?.let {
                        handler.error(it)
                    } ?: kotlin.run {
                        if (isSuccessful) {
                            content?.let { handler.success(JSONObject(String(it))) }
                        } else {
                            handler.error(Exception())
                        }
                    }
                }
            })
    }

    /**
     * Télécharge et stocke l'icon associé à la metéo
     * @param url url de l'image à telecharger
     * @param path chemin ou l'image sera enregistrée
     */
    fun findWeatherIcon(url: String, path: String, handler: WeatherIconHandler) {
        thread(start = true) {
            try {
                val file = File(path)
                var bitmap: Bitmap
                if (!file.exists()) {
                    URL(url).openConnection().getInputStream().apply {
                        bitmap = BitmapFactory.decodeStream(this)
                        thread(start = true) { file.writeBytes(this.readBytes()) }
                    }
                    /*BitmapFactory.decodeStream(URL(url).openConnection().getInputStream()).apply {
                        thread(start = true) { saveBitmap(this, path) }
                    }*/
                } else {
                    bitmap = BitmapFactory.decodeFile(path)
                }
                handler.success(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                handler.error(e)
            }
        }
    }

    /**
     * Listener dédié à la recuperation de données meteologiques
     */
    interface WeatherDataHandler {
        fun success(response: JSONObject)
        fun error(exception: Exception)
    }

    /**
     * Listener dédié à la recuperation de données meteologiques
     */
    interface WeatherIconHandler {
        fun success(bitmap: Bitmap)
        fun error(exception: Exception)
    }
}