package com.beautifulapp.basis_adk.helper.services

import android.net.Uri
import android.util.Log
import com.beautifulapp.basis_adk.helper.database.DataBase
import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass
import com.beautifulapp.basis_adk.helper.pattern.CustomDataClass.Companion.ID
import com.beautifulapp.basis_adk.helper.pattern.Table
import com.beautifulapp.basis_adk.helper.pattern.TableGen
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.InputStream

object FireStoreHelper {
    val TAG = "FireStoreHelper"
    val db by lazy { FirebaseFirestore.getInstance() }
    val storage by lazy { FirebaseStorage.getInstance() }


    fun post(data: CustomDataClass, listener: ((Boolean, TableGen?) -> Unit)? = null) {
        with(data) {
            var did = objectId
            val cr = db.collection(className)
            val savedMap = commit()
            when (did) {
                null -> {
                    cr.document().apply { did = id }.set(savedMap.apply { put(ID, did) })
                }
                "" -> {
                    cr.document().apply { did = id }.set(savedMap.apply { put(ID, did) })
                }
                else -> {
                    cr.document(did!!).set(savedMap)//modif 23/07
                }
            }.addOnCompleteListener {
                listener?.invoke(it.isSuccessful, if (it.isSuccessful) savedMap else null)
            }
        }
    }


    fun postWithUpload(
        data: CustomDataClass,
        uploadAction: ((String, (CustomDataClass?) -> Unit) -> Unit)? = null,
        listener: ((Boolean, TableGen?) -> Unit)? = null
    ) {
        with(data) {
            var did = objectId
            var savedMap = Table()
            val doc = when (did) {
                null, "" -> db.collection(className).document()
                else -> db.collection(className).document(did)
            }

            doc.let {
                did = it.id
                uploadAction?.let { ua ->
                    ua.invoke(did!!) { cdc ->
                        savedMap = (cdc ?: (this@with)).commit().apply { put(ID, did) }
                        it.set(savedMap).addOnCompleteListener { task ->
                            listener?.invoke(task.isSuccessful, if (task.isSuccessful) savedMap else null)
                        }
                    }
                } ?: kotlin.run {
                    savedMap = (this@with).commit().apply { put(ID, did) }
                    it.set(savedMap).addOnCompleteListener { task ->
                        listener?.invoke(task.isSuccessful, if (task.isSuccessful) savedMap else null)
                    }
                }
            }
        }
    }

    fun safePost(data: CustomDataClass, listener: PostListener? = null) {
        with(data) {
            db.collection(className)
                .whereEqualTo(FieldPath.documentId(), objectId)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.documents?.let { docs ->
                            docs.forEach { doc ->
                                db.batch().update(doc.reference, this/*map*/)
                            }
                            listener?.succeed()
                        } ?: kotlin.run {
                            db.collection(className).add(this/*map*/).addOnCompleteListener { it2 ->
                                if (it2.isSuccessful) {
                                    listener?.succeed()
                                    Log.e(TAG, "result: $it2")
                                } else {
                                    listener?.failed()
                                }
                            }
                        }
                    } else listener?.failed()
                }
        }
    }


    fun getList(
        className: String,
        where: Collection<Triple<String, DataBase.Where, Any>>? = null,
        listener: ((Boolean, MutableList<MutableMap<String, Any>?>?) -> Unit)? = null
    ) {
        var q: Query = db.collection(className)
        where?.forEach {
            q = when (it.second) {
                DataBase.Where.Equal -> q.whereEqualTo(it.first, it.third)
                DataBase.Where.Sup -> q.whereGreaterThan(it.first, it.third)
                DataBase.Where.SupEqual ->
                    q.whereGreaterThanOrEqualTo(it.first, it.third)
                DataBase.Where.Inf -> q.whereLessThan(it.first, it.third)
                DataBase.Where.InfEqual -> q.whereLessThanOrEqualTo(it.first, it.third)
                DataBase.Where.ArrayContain -> q.whereArrayContains(it.first, it.third)
                DataBase.Where.sortedBy ->
                    q.orderBy(it.first, if (it.third == "+") Query.Direction.ASCENDING else Query.Direction.DESCENDING)
            }
        }

        /*val t=try{ Tasks.await(q.get()).documents.map { doc -> doc.data }.toMutableList()
        }catch (e: Exception){null}*/

        q.get()
            .addOnCompleteListener {
                listener?.invoke(it.isSuccessful, it.result?.documents?.map { doc -> doc.data }?.toMutableList())
            }
    }

    fun <T : CustomDataClass> getList(
        clazz: Class<T>,
        network: Boolean = true,
        where: Collection<Triple<String, DataBase.Where, Any>>? = null,
        listener: ((Boolean, MutableList<T>?) -> Unit)? = null
    ) {
        var q: Query = db.collection(clazz.simpleName)
        where?.forEach {
            q = when (it.second) {
                DataBase.Where.Equal -> q.whereEqualTo(it.first, it.third)
                DataBase.Where.Sup -> q.whereGreaterThan(it.first, it.third)
                DataBase.Where.SupEqual -> q.whereGreaterThanOrEqualTo(it.first, it.third)
                DataBase.Where.Inf -> q.whereLessThan(it.first, it.third)
                DataBase.Where.InfEqual -> q.whereLessThanOrEqualTo(it.first, it.third)
                DataBase.Where.ArrayContain -> q.whereArrayContains(it.first, it.third)
                DataBase.Where.sortedBy -> q.orderBy(it.first, if (it.third == "+") Query.Direction.ASCENDING else Query.Direction.DESCENDING)
            }
        }
        Log.e("FireBase getList", "${this}")

        q.get(if (network) Source.SERVER else Source.CACHE)
            .addOnCompleteListener { t ->
                Log.e("FireBase getList", "${if (network) Source.SERVER else Source.CACHE} : ${if (t.isSuccessful) t.result?.documents?.size else null}")
                listener?.invoke(t.isSuccessful, if (!t.isSuccessful) null else
                    t.result?.documents?.mapNotNull { doc ->
                        doc.data?.let {
                            CustomDataClass.create(
                                HashMap(it),
                                clazz
                            )
                        }
                    }?.toMutableList()
                )
            }

    }

    fun getItem(className: String, objectId: String, listener: ((Boolean, HashMap<String, Any?>?) -> Unit)? = null) {
        db.collection(className).document(objectId)
            .get()
            .addOnCompleteListener { task ->
                listener?.invoke(task.isSuccessful, task.result?.data?.let { HashMap(it) })
            }
    }

    fun <T : CustomDataClass> getItem(clazz: Class<T>, network: Boolean = true, objectId: String, listener: ((Boolean, T?) -> Unit)? = null) {
        db.collection(clazz.simpleName).document(objectId)
            .get(if (network) Source.SERVER else Source.CACHE)
            .addOnCompleteListener { task ->
                listener?.invoke(
                    task.isSuccessful,
                    if (!task.isSuccessful) null else task.result?.data?.let {
                        CustomDataClass.create(
                            HashMap(it),
                            clazz
                        )
                    })
            }
    }

    fun <T : CustomDataClass> remove(clazz: Class<T>, objectId: String, listener: ((Boolean) -> Unit)?) {
        db.collection(clazz.simpleName).document(objectId)
            .delete()
            .addOnCompleteListener { task ->
                listener?.invoke(task.isSuccessful)
            }
    }

    fun <T : CustomDataClass> removeIncludeMedias(clazz: Class<T>, objectId: String, mediaPath: Collection<String>? = null, listener: ((Boolean) -> Unit)?) {
        mediaPath?.forEach {
            deleteMedia(
                it
            )
        }

        db.collection(clazz.simpleName).document(objectId)
            .delete()
            .addOnCompleteListener { task ->
                listener?.invoke(task.isSuccessful)
            }
    }


    /**
     * A verifier
     */
    fun upload(to: String, rawData: ByteArray? = null, inputStream: InputStream? = null, file: File? = null, callback: ((Boolean, String) -> Unit)? = null) {
        val storageRef = storage.reference.child(to)
        if (rawData != null) {
            storageRef.putBytes(rawData).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation storageRef.downloadUrl
            })
                .addOnCompleteListener {
                    Log.e(TAG, "upload: ${it.result?.toString()}")
                    callback?.invoke(it.isSuccessful, it.result.toString())
                    Log.e(TAG, "upload dans complete: ")

                }
        }

        if (file != null) {
            storageRef.putFile(Uri.fromFile(file)).addOnCompleteListener {
                callback?.invoke(it.isSuccessful, storageRef.downloadUrl.result.toString())
                Log.e(TAG, "upload: ${storageRef.downloadUrl.result.toString()}")
            }
        }

        if (inputStream != null) {
            storageRef.putStream(inputStream).addOnCompleteListener {
                callback?.invoke(it.isSuccessful, storageRef.downloadUrl.result.toString())
                Log.e(TAG, "upload: ${storageRef.downloadUrl.result.toString()}")
            }
        }

    }

    /**
     * A revoir
     */
    fun download(to: String, file: File? = null, rawDataSize: Long? = null, inputStream: InputStream? = null) {
        val storageRef = storage.reference.child(to)
        when {
            (rawDataSize != null) -> {
                storageRef.getBytes(rawDataSize).addOnCompleteListener {
                    if (it.isSuccessful) it.result
                }
            }
            (file != null) -> {
                storageRef.getFile(file).addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result
                    }
                }
            }
            (inputStream != null) -> {
                storageRef.getStream { taskSnapshot, inputStream ->

                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.stream
                    }
                }
            }
            else -> null
        }?.addOnCompleteListener {

        }
    }

    fun deleteMedia(to: String, callback: ((Boolean) -> Unit)? = null) {
        storage.getReferenceFromUrl(to)
            .delete()
            .addOnCompleteListener {
                callback?.invoke(it.isSuccessful)
            }
    }

    enum class Where(key: String) {
        Equal("="),
        Sup(">"),
        SupEqual(">="),
        Inf("<"),
        InfEqual("<="),
        ArrayContain("C"),
        sortedBy("**")
    }

    interface GetListener {
        fun succeed(documents: MutableList<DocumentSnapshot>?)

        fun failed()
    }

    interface PostListener {
        fun succeed()

        fun failed()
    }
}