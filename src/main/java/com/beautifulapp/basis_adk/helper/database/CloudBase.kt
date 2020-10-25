package com.beautifulapp.basis_adk.helper.database


import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass
import com.beautifulapp.basis_adk.helper.services.FireStoreHelper
import java.io.File
import java.io.InputStream

class CloudBase : DataBase {
    val db = FireStoreHelper.db

    override fun <T : CustomDataClass> getItem(clazz: Class<T>, objectId: String, listener: ((Boolean, T?) -> Unit)?) {
        FireStoreHelper.getItem(
            clazz,
            true,
            objectId,
            listener
        )
    }

    override fun <T : CustomDataClass> getList(clazz: Class<T>, where: Collection<Triple<String, DataBase.Where, Any>>?, listener: ((Boolean, MutableList<T>?) -> Unit)?) {
        FireStoreHelper.getList(
            clazz,
            where = where,
            listener = listener
        )
    }

    override fun <T : CustomDataClass> remove(clazz: Class<T>, objectId: String, listener: ((Boolean) -> Unit)?) {
        FireStoreHelper.remove(
            clazz,
            objectId,
            listener
        )
    }

    override fun getItem(className: String, objectId: String, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        FireStoreHelper.getItem(
            className,
            objectId,
            listener
        )
    }

    override fun getList(className: String, where: Collection<Triple<String, DataBase.Where, Any>>?, listener: ((Boolean, MutableList<MutableMap<String, Any>?>?) -> Unit)?) {
        FireStoreHelper.getList(
            className,
            where,
            listener
        )
    }

    override fun post(data: CustomDataClass, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        FireStoreHelper.post(data, listener)
    }

    override fun upload(to: String, rawData: ByteArray?, inputStream: InputStream?, file: File?, listener: ((Boolean, String) -> Unit)?) {
        FireStoreHelper.upload(
            to,
            rawData,
            inputStream,
            file,
            listener
        )
    }

    override fun postWithUpload(data: CustomDataClass, uploadAction: ((String, (CustomDataClass?) -> Unit) -> Unit)?, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)?) {
        FireStoreHelper.postWithUpload(
            data,
            uploadAction,
            listener
        )
    }

    override fun <T : CustomDataClass> removeIncludeMedias(clazz: Class<T>, objectId: String, mediaPath: Collection<String>?, listener: ((Boolean) -> Unit)?) {
        FireStoreHelper.removeIncludeMedias(
            clazz,
            objectId,
            mediaPath,
            listener
        )
    }
}