package com.beautifulapp.basis_adk.helper.database

import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass
import java.io.File
import java.io.InputStream

/**
 * Interface minimaliste pour decrivant les methodes d'une base de données
 */
interface DataBase {

    fun getItem(className: String, objectId: String, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)? = null)
    fun <T : CustomDataClass> getItem(clazz: Class<T>, objectId: String, listener: ((Boolean, T?) -> Unit)? = null)

    fun <T : CustomDataClass> remove(clazz: Class<T>, objectId: String, listener: ((Boolean) -> Unit)? = null)

    fun getList(
        className: String,
        where: Collection<Triple<String, Where, Any>>? = null,
        listener: ((Boolean, MutableList<MutableMap<String, Any>?>?) -> Unit)? = null
    )

    fun <T : CustomDataClass> getList(
        clazz: Class<T>, where: Collection<Triple<String, Where, Any>>? = null,
        listener: ((Boolean, MutableList<T>?) -> Unit)? = null
    )

    fun post(data: CustomDataClass, listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)? = null)

    fun upload(to: String, rawData: ByteArray? = null, inputStream: InputStream? = null, file: File? = null, listener: ((Boolean, String) -> Unit)? = null)

    fun postWithUpload(
        data: CustomDataClass,
        uploadAction: ((String, (CustomDataClass?) -> Unit) -> Unit)? = null,
        listener: ((Boolean, MutableMap<String, Any?>?) -> Unit)? = null
    )

    fun <T : CustomDataClass> removeIncludeMedias(clazz: Class<T>, objectId: String, mediaPath: Collection<String>? = null, listener: ((Boolean) -> Unit)?)

    enum class Where(key: String) {
        Equal("="),
        Sup(">"),
        SupEqual(">="),
        Inf("<"),
        InfEqual("<="),
        ArrayContain("C"),
        sortedBy("**"),
    }
}