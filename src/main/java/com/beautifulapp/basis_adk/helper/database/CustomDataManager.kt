package com.beautifulapp.basis_adk.helper.database

import android.app.Application
import android.util.Log
import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass
import com.beautifulapp.basis_adk.helper.pattern.TableGen
import com.beautifulapp.basis_adk.helper.services.FireStoreHelper
import java.io.File
import java.io.InputStream

/**
 * Cette object a pour but de gerer et reguler tous les transferts de données consernant des CustomDataClass
 * @since 02/09/2019
 */
object CustomDataManager : HashMap<String, CustomDataClass>() {
    val TAG = "CustomDataManager"
    var localBD: DataBase? = null
    var cloudBD: DataBase? = null
    var currentCloudResquest = mutableSetOf<String>()

    fun initialize(app: Application, localBD: DataBase? = null, cloudBD: DataBase? = null) {
        CustomDataManager.localBD = localBD
        CustomDataManager.cloudBD = cloudBD
    }

    /**
     * Ecriture de [CustomDataClass] vers la [Source] definie
     */
    fun save(destination: Source, customDataClass: CustomDataClass, callback: ((Boolean, TableGen?) -> Unit)? = null) {
        Log.e(TAG, "save: ${customDataClass::className}")
        //customDataClass.map=customDataClass.commit()
        customDataClass.putAll(customDataClass.commit())
        when (destination) {
            Source.Local -> {
                localBD?.post(customDataClass, callback)
            }

            Source.Cloud -> {
                cloudBD?.post(customDataClass, callback)
            }

            Source.Runtime -> {
                (customDataClass.javaClass.simpleName + (customDataClass.objectId ?: customDataClass.hashCode().toString())).let {
                    this[it] = customDataClass
                    callback?.invoke(true, customDataClass)
                } ?: kotlin.run {
                    callback?.invoke(true, customDataClass)
                }
            }

            Source.All -> {
                cloudBD?.post(customDataClass, callback)
                localBD?.post(customDataClass, callback)
            }
        }
    }

    /**
     * Ecriture de [CustomDataClass] vers la [Source] definie
     */
    fun save(
        destination: Source,
        customDataClass: CustomDataClass,
        uploadAction: ((String, (CustomDataClass?) -> Unit) -> Unit)? = null,
        callback: ((Boolean, TableGen?) -> Unit)? = null
    ) {
        Log.e(TAG, "save: ${customDataClass::className}")
        //customDataClass.map=customDataClass.commit()
        customDataClass.putAll(customDataClass.commit())
        when (destination) {
            Source.Local -> {
                localBD?.postWithUpload(customDataClass, uploadAction, callback)
            }

            Source.Cloud -> {
                cloudBD?.postWithUpload(
                    customDataClass, uploadAction,
                    convertCallBack(
                        customDataClass,
                        callback
                    )
                )
            }

            Source.Runtime -> {
                (customDataClass.javaClass.simpleName + (customDataClass.objectId ?: customDataClass.hashCode().toString())).let {
                    this[it] = customDataClass
                    callback?.invoke(true, customDataClass)
                } ?: kotlin.run {
                    callback?.invoke(true, customDataClass)
                }
            }

            Source.All -> {
                cloudBD?.postWithUpload(customDataClass, uploadAction, callback)
                localBD?.postWithUpload(customDataClass, uploadAction, callback)
            }
        }
    }

    fun <T : CustomDataClass> delete(source: Source, clazz: Class<T>, objectId: String, mediaPaths: Collection<String>? = null, listener: ((Boolean) -> Unit)? = null) {
        when (source) {
            Source.Local -> {
                localBD?.removeIncludeMedias(clazz, objectId, mediaPaths) { b -> listener?.invoke(b.apply { this@CustomDataManager.remove(clazz.simpleName + objectId) }) }
            }

            Source.Cloud, Source.Cache -> {
                if (currentCloudResquest.contains(clazz.simpleName + objectId)) return
                cloudBD?.removeIncludeMedias(clazz, objectId, mediaPaths) { b -> listener?.invoke(b.apply { this@CustomDataManager.remove(clazz.simpleName + objectId) }) }
            }


            Source.Runtime -> {
                this@CustomDataManager.remove(clazz.simpleName + objectId)
                listener?.invoke(true)
            }

            Source.All -> {
                cloudBD?.removeIncludeMedias(clazz, objectId, mediaPaths) { b -> listener?.invoke(b.apply { this@CustomDataManager.remove(clazz.simpleName + objectId) }) }
                localBD?.removeIncludeMedias(clazz, objectId, mediaPaths) { b -> listener?.invoke(b.apply { this@CustomDataManager.remove(clazz.simpleName + objectId) }) }
            }
        }
    }


    /**
     * Lecture d'une [CustomDataClass] depuis une [Source] definie
     */
    private fun getItem(source: Source, className: String, objectId: String, listener: ((Boolean, TableGen?) -> Unit)? = null) {
        when (source) {
            Source.Local -> {
                localBD?.getItem(className, objectId, listener)
            }

            Source.Cloud -> {
                cloudBD?.getItem(className, objectId, listener)
            }

            Source.All -> {
                cloudBD?.getItem(className, objectId, listener)
                localBD?.getItem(className, objectId, listener)
            }
        }
    }

    fun <T : CustomDataClass> getItem(source: Source, clazz: Class<T>, objectId: String, listener: ((Boolean, T?) -> Unit)? = null) {
        Log.e(TAG, "getItem: ${clazz}")
        when (source) {
            Source.Local -> {
                localBD?.getItem(clazz, objectId) { b, it -> it?.let { d -> this[clazz.simpleName + objectId] = d };listener?.invoke(b, it) }
            }

            Source.Cloud -> {
                if (currentCloudResquest.contains(clazz.simpleName + objectId)) return
                cloudBD?.getItem(clazz, objectId) { b, it ->
                    it?.let { d -> this[clazz.simpleName + objectId] = d };convertCallBack(
                    clazz,
                    objectId,
                    listener
                )?.invoke(b, it)
                }
            }

            Source.Cache -> {
                if (currentCloudResquest.contains(clazz.simpleName + objectId)) return
                FireStoreHelper.getItem(clazz, false, objectId) { b, it ->
                    it?.let { d -> this[clazz.simpleName + objectId] = d };convertCallBack(
                    clazz,
                    objectId,
                    listener
                )?.invoke(b, it)
                }
                //cloudBD?.getItem(clazz, objectId) { b, it -> it?.let { d -> this[clazz.simpleName + objectId] = d };convertCallBack(clazz, objectId, listener)?.invoke(b, it) }
            }

            Source.Runtime -> {
                listener?.invoke(true, (get(clazz.simpleName + objectId) ?: get(clazz.simpleName + hashCode().toString())) as? T)
            }

            Source.All -> {
                cloudBD?.getItem(clazz, objectId) { b, it -> it?.let { d -> this[clazz.simpleName + objectId] = d };listener?.invoke(b, it) }
                localBD?.getItem(clazz, objectId) { b, it -> it?.let { d -> this[clazz.simpleName + objectId] = d };listener?.invoke(b, it) }
            }
        }
    }

    /**
     * Lecture d'une Liste de [CustomDataClass] depuis une [Source] definie avec des critéres([DataBase.Where]) definis
     */
    private fun getList(
        source: Source, className: String, where: Collection<Triple<String, DataBase.Where, Any>>? = null,
        listener: ((Boolean, MutableList<MutableMap<String, Any>?>?) -> Unit)? = null
    ) {
        when (source) {
            Source.Local -> {
                localBD?.getList(className, where, listener)
            }

            Source.Cloud -> {
                cloudBD?.getList(className, where, listener)
            }

            Source.All -> {
                cloudBD?.getList(className, where, listener)
                localBD?.getList(className, where, listener)
            }
        }
    }

    fun <T : CustomDataClass> getList(
        source: Source, clazz: Class<T>, where: Collection<Triple<String, DataBase.Where, Any>>? = null,
        listener: ((Boolean, MutableList<T>?) -> Unit)? = null
    ) {
        Log.e(TAG, "getList: ${clazz}")
        when (source) {
            Source.Local -> {
                localBD?.getList(clazz, where) { b, it -> it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };listener?.invoke(b, it) }
            }

            Source.Cloud -> {
                if (currentCloudResquest.contains(
                        clazz.simpleName + where?.hashCode().toString()
                    )
                ) return
                cloudBD?.getList(clazz, where) { b, it ->
                    it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };convertCallBack(
                    clazz,
                    where,
                    listener
                )?.invoke(b, it)
                }
            }

            Source.Cache -> {
                if (currentCloudResquest.contains(clazz.simpleName + where?.hashCode().toString())) return
                FireStoreHelper.getList(clazz, false, where) { b, it ->
                    it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };convertCallBack(
                    clazz,
                    where,
                    listener
                )?.invoke(b, it)
                }
                //cloudBD?.getList(clazz, where) { b, it -> it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };convertCallBack(clazz, where, listener)?.invoke(b, it) }
            }

            Source.All -> {
                cloudBD?.getList(clazz, where) { b, it -> it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };listener?.invoke(b, it) }
                localBD?.getList(clazz, where) { b, it -> it?.forEach { o -> this[clazz.simpleName + o.objectId!!] = o };listener?.invoke(b, it) }
            }
        }
    }

    fun upload(source: Source, to: String, rawData: ByteArray? = null, inputStream: InputStream? = null, file: File? = null, listener: ((Boolean, String) -> Unit)?) {
        when (source) {
            Source.Local -> {
                localBD?.upload(to, rawData, inputStream, file, listener)
            }

            Source.Cloud -> {
                cloudBD?.upload(to, rawData, inputStream, file, listener)
            }

            Source.All -> {
                cloudBD?.upload(to, rawData, inputStream, file, listener)
                localBD?.upload(to, rawData, inputStream, file, listener)
            }
        }
    }

    fun convertCallBack(customDataClass: CustomDataClass, src: ((Boolean, TableGen?) -> Unit)?): ((Boolean, TableGen?) -> Unit)? {
        return { b: Boolean, table: TableGen? ->
            val hash = customDataClass.hashCode().toString()
            currentCloudResquest.add(hash)
            src?.invoke(b, table)
            currentCloudResquest.remove(hash)
        }
    }

    fun <T : CustomDataClass> convertCallBack(
        clazz: Class<T>,
        where: Collection<Triple<String, DataBase.Where, Any>>?,
        src: ((Boolean, MutableList<T>?) -> Unit)?
    ): ((Boolean, MutableList<T>?) -> Unit)? {
        return { b: Boolean, list: MutableList<T>? ->
            val hash = clazz.simpleName + where?.hashCode().toString()
            currentCloudResquest.add(hash)
            src?.invoke(b, list)
            currentCloudResquest.remove(hash)
        }
    }

    fun <T : CustomDataClass> convertCallBack(clazz: Class<T>, id: String, src: ((Boolean, T?) -> Unit)?): ((Boolean, T?) -> Unit)? {
        return { b: Boolean, item: T? ->
            val hash = clazz.simpleName + id
            currentCloudResquest.add(hash)
            src?.invoke(b, item)
            currentCloudResquest.remove(hash)
        }
    }

    enum class Source {
        Local,
        Cloud,
        All,
        Runtime,
        Cache
    }
}