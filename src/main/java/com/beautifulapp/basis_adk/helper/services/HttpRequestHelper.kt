package com.beautifulapp.basis_adk.helper.services


import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.URLConnection


object HttpRequestHelper {

    fun get(url: String, listener: HttpRequestListener?) {
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener?.onCompleted(false, exception = e)
            }

            override fun onResponse(call: Call, response: Response) {
                listener?.onCompleted(response.isSuccessful, response.body()?.bytes(), response.body()?.contentType())
                response.body()?.close()
            }
        })
    }

    fun post(url: String, fields: MutableMap<String, String>? = null, files: MutableMap<String, File>? = null, listener: HttpRequestListener?) {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            fields?.forEach {
                addFormDataPart(it.key, it.value)
            }
            files?.forEach {
                addFormDataPart(it.key, it.value.name, RequestBody.create(MediaType.parse(URLConnection.guessContentTypeFromName(it.value.name)), it.value))
            }
        }.build()

        val request = Request.Builder().url(url).post(requestBody).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener?.onCompleted(false, exception = e)
            }

            override fun onResponse(call: Call, response: Response) {
                listener?.onCompleted(response.isSuccessful, response.body()?.bytes(), response.body()?.contentType())
                response.body()?.close()
            }
        })
    }

    interface HttpRequestListener {
        fun onCompleted(isSuccessful: Boolean, content: ByteArray? = null, contentType: MediaType? = null, exception: IOException? = null)
    }

}