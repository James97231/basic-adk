package com.beautifulapp.basis_adk.helper.database

import android.Manifest
import androidx.annotation.RequiresPermission
import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass
import com.google.gson.Gson
import java.io.File
import java.io.InputStream
import kotlin.concurrent.thread

class LocalBase : DataBase {
    override fun <T : CustomDataClass> getList(clazz: Class<T>, where: Collection<Triple<String, DataBase.Where, Any>>?, listener: ((Boolean, MutableList<T>?) -> Unit)?) {
        thread(start = true) {
            File(appPath + File.separator + clazz.simpleName).listFiles().mapNotNull { f ->
                (Gson().fromJson(f.readText(), MutableMap::class.java) as? MutableMap<String, Any?>)?.let {
                    CustomDataClass.create(
                        it,
                        clazz
                    )
                }
            }.toMutableList().apply {
                listener?.invoke(true, this)
            }
        }
    }

    override fun <T : CustomDataClass> getItem(clazz: Class<T>, objectId: String, listener: ((Boolean, T?) -> Unit)?) {
        thread(start = true) {
            File(appPath + File.separator + clazz.simpleName + File.separator + objectId + ".json").apply {
                listener?.invoke(
                    exists(),
                    if (exists()) {
                        (Gson().fromJson(File(path).readText(), MutableMap::class.java) as? MutableMap<String, Any?>)?.let {
                            //clazz.kotlin.primaryConstructor?.call(it)
                            CustomDataClass.create(
                                it,
                                clazz
                            )
                        }
                    } else null
                )
            }
        }
    }

    override fun <T : CustomDataClass> remove(clazz: Class<T>, objectId: String, listener: ((Boolean) -> Unit)?) {
        thread(start = true) {
            File(appPath + File.separator + clazz.simpleName + File.separator + objectId + ".json").apply {
                listener?.invoke(delete())
            }
        }
    }

    val TAG = "LocalBase"
    var appPath: String

    constructor(appPath: String) {
        this.appPath = appPath
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun getItem(className: String, objectId: String, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        thread(start = true) {
            File(appPath + File.separator + className + File.separator + objectId + ".json").apply {
                listener?.invoke(
                    exists(),
                    if (exists()) {
                        (Gson().fromJson(readText(), MutableMap::class.java) as? MutableMap<String, Any?>)
                    } else null
                )
            }
        }
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun getList(className: String, where: Collection<Triple<String, DataBase.Where, Any>>?, listener: ((Boolean, MutableList<MutableMap<String, Any>?>?) -> Unit)?) {
        thread(start = true) {
            File(appPath + File.separator + className).listFiles().map {
                (Gson().fromJson(it.readText(), MutableMap::class.java) as? MutableMap<String, Any>)
            }.toMutableList().apply {
                listener?.invoke(true, this)
            }


            /*File(appPath + File.separator + className).apply {
                listener?.invoke(listFiles().map {
                    (Gson().fromJson(it.readText(), MutableMap::class.java) as? MutableMap<String, Any>)
                }.toMutableList())
            }*/
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun post(data: CustomDataClass, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        thread(start = true) {
            val path = appPath + File.separator + data.className + File.separator + (data.objectId ?: data.hashCode()) + ".json"
            File(path).apply { parentFile.mkdirs() }.writeText(data.toJson().toString(5))
            listener?.invoke(true, data)
            createTempFile()
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun upload(to: String, rawData: ByteArray?, inputStream: InputStream?, file: File?, listener: ((Boolean, String) -> Unit)?) {
        thread(start = true) {
            val path = appPath + File.separator + "storage" + File.separator + to
            val bytes = rawData ?: inputStream?.readBytes() ?: file?.readBytes()
            bytes?.let {
                File(path).apply { parentFile.mkdirs() }.writeBytes(bytes!!)
                listener?.invoke(true, path)
            } ?: kotlin.run { listener?.invoke(false, path) }

        }
    }

    override fun postWithUpload(data: CustomDataClass, uploadAction: ((String, (CustomDataClass?) -> Unit) -> Unit)?, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : CustomDataClass> removeIncludeMedias(clazz: Class<T>, objectId: String, mediaPath: Collection<String>?, listener: ((Boolean) -> Unit)?) {
        TODO("Not yet implemented")
    }
}