package com.beautifulapp.basis_adk.helper.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.beautifulapp.basis_adk.getPdfPreview
import com.beautifulapp.basis_adk.imageRotationFromUri
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.IOException
import java.net.URL

object BitmapDrawableCache : HashMap<String, MutableLiveData<BitmapDrawable>>() {
    init {
        withDefault {
            MutableLiveData()
        }
    }


    @JvmStatic
    fun addFromPath(c: Context, path: String): MutableLiveData<BitmapDrawable> {
        if (!containsKey(path)) {
            this[path] = MutableLiveData()

            GlobalScope.async {
                this@BitmapDrawableCache[path]?.apply {
                    postValue(BitmapDrawable(c.resources, path))
                }
            }
        }
        return this[path]!!
    }

    @JvmStatic
    fun addFromUri(context: Context, id: String, uri: Uri): MutableLiveData<BitmapDrawable> {
        if (!containsKey(id)) {
            this[id] = MutableLiveData()
            GlobalScope.async {
                this@BitmapDrawableCache[id]?.apply {
                    postValue(
                        bitmapFromUri(
                            context,
                            uri
                        )
                    )
                }
            }
        }
        return this[id]!!
    }


    @JvmStatic
    fun addFromUrl(c: Context, url: String?): MutableLiveData<BitmapDrawable> {
        url?.let { s ->
            if (!containsKey(s)) {
                this[s] = MutableLiveData()
                GlobalScope.async {
                    this@BitmapDrawableCache[s]?.apply {
                        try {
                            postValue(
                                bitmapFromUrl(
                                    c,
                                    s
                                )
                            )
                        } catch (z: IOException) {
                        }
                    }
                }
            }
            return this[s]!!
        } ?: kotlin.run {
            return MutableLiveData()
        }
    }


    @JvmStatic
    fun addFromBitmap(c: Context, id: String, bitmap: Bitmap): MutableLiveData<BitmapDrawable> {
        if (!containsKey(id)) {
            this[id] = MutableLiveData(BitmapDrawable(c.resources, bitmap))
        }
        return this[id]!!
    }

    @JvmStatic
    fun addFromBitmapDrawable(id: String, b: BitmapDrawable): MutableLiveData<BitmapDrawable> {
        if (!containsKey(id)) {
            this[id] = MutableLiveData(b)
        }
        return this[id]!!
    }

    @JvmStatic
    suspend fun bitmapFromAll(c: Context, url: String?): BitmapDrawable? {
        return url?.let {
            try {
                val u = Uri.parse(it)
                when (u.scheme) {
                    "http", "https", "ftp" -> bitmapFromUrl(
                        c,
                        it
                    )
                    null -> BitmapDrawable(c.resources, it)
                    else -> bitmapFromUri(
                        c,
                        u
                    )
                }

            } catch (e: IOException) {
                null
            }
        }
    }

    @JvmStatic
    fun bitmapFromUrl(c: Context, url: String?): BitmapDrawable? {
        return url?.let {
            try {
                if (url.endsWith(".pdf")) {
                    BitmapDrawable(
                        c.resources,
                        getPdfPreview(URL(url).readBytes(), c.cacheDir.path)
                    )
                } else {
                    BitmapDrawable(c.resources, URL(it).openStream())
                }

                //BitmapDrawable(c.resources, URL(it).openStream())
            } catch (e: IOException) {
                null
            }
        }
    }

    @JvmStatic
    fun bitmapFromUri(context: Context, uri: Uri): BitmapDrawable? {
        val matrix = Matrix().apply {
            postRotate(imageRotationFromUri(context, uri).toFloat())
        }

        return context.contentResolver.openInputStream(uri)?.let {
            val bitmap = BitmapFactory.decodeStream(it)
            it.close()
            bitmap ?: return null
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            matrix.postScale((1080 * ratio) / bitmap.width, 1080f / bitmap.height)

            try {
                val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                BitmapDrawable(context.resources, bitmap2)
            } catch (e: OutOfMemoryError) {
                null
            }
        }
    }


}