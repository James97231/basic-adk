package com.beautifulapp.basis_adk.helper.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread


object BitmapCache : HashMap<String, MutableLiveData<Bitmap>>() {
    init {
        withDefault {
            MutableLiveData()
        }
    }


    @JvmStatic
    fun addFromPath(path: String): MutableLiveData<Bitmap> {
        if (!containsKey(path)) {
            this[path] = MutableLiveData()
            thread(start = true) {
                this[path]?.apply {
                    postValue(BitmapFactory.decodeFile(path))
                }
            }
        }
        return this[path]!!
    }

    @JvmStatic
    fun addFromUri(context: Context, id: String, uri: Uri): MutableLiveData<Bitmap> {
        if (!containsKey(id)) {
            this[id] = MutableLiveData()
            thread(start = true) {
                this[id]?.apply {
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
    fun addFromUrl(url: String?): MutableLiveData<Bitmap> {
        return url?.let { s ->
            if (!containsKey(url)) {
                this[s] = MutableLiveData()
                thread(start = true) {
                    this[s]?.apply {
                        try {
                            URL(s).openStream()?.also {
                                postValue(BitmapFactory.decodeStream(it))
                            }
                        } catch (z: IOException) {
                        }

                    }
                }
            }
            this[url]!!
        } ?: MutableLiveData()
    }

    @JvmStatic
    fun addFromUrlToB(c: Context, url: String?): MutableLiveData<BitmapDrawable> {
        url?.let { s ->
            if (!containsKey(url)) {
                this[s] = MutableLiveData()
                thread(start = true) {
                    this[s]?.apply {
                        try {
                            URL(s).openStream()?.also {
                                postValue(BitmapFactory.decodeStream(it))
                            }
                        } catch (z: IOException) {
                        }

                    }
                }
            }

            return MediatorLiveData<BitmapDrawable>().apply {
                addSource(this@BitmapCache[url]!!) {
                    value = BitmapDrawable(c.resources, it)
                }
            }

        } ?: kotlin.run {
            return MutableLiveData()
        }
    }


    @JvmStatic
    fun addFromBitmap(id: String, bitmap: Bitmap): MutableLiveData<Bitmap> {
        if (!containsKey(id)) {
            this[id] = MutableLiveData(bitmap)
        }
        return this[id]!!
    }

    @JvmStatic
    fun bitmapFromUrl(url: String?): Bitmap? {
        return url?.let {
            BitmapFactory.decodeStream(URL(it).openStream())
        }
    }

    @JvmStatic
    fun bitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.let {
            val bitmap = BitmapFactory.decodeStream(it)
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val exifInterface =
                try {
                    PathGetter.getPath(context, uri)?.let { path ->
                        ExifInterface(path)
                    } ?: if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) ExifInterface(it) else null
                } catch (e: Exception) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) ExifInterface(it) else null
                }

            val matrix = Matrix().apply {
                when (exifInterface!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                    else -> postRotate(0f)
                }
                postScale((1080 * ratio) / bitmap.width, 1080f / bitmap.height)
            }

            try {
                val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap?.recycle()
                bitmap2
            } catch (e: OutOfMemoryError) {
                null
            }

        }


        /*return context.contentResolver.openInputStream(uri)?.let {
            BitmapFactory.decodeStream(it)
        }*/
    }

}