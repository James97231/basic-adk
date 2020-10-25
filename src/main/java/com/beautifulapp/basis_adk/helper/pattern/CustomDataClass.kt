package com.beautifulapp.basis_adk.helper.pattern


import android.util.Log
import com.beautifulapp.basis_adk.helper.database.CustomDataManager
import org.json.JSONObject
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

typealias Table = HashMap<String, Any?>
typealias TableGen = MutableMap<String, Any?>

abstract class CustomDataClass(m: TableGen) : LinkedHashMap<String, Any?>(m) {
    //abstract val map: Table
    abstract val className: String
    abstract val objectId: String?

    inline operator fun <reified T> getValue(thisRef: Any?, prop: KProperty<*>) = get(prop.name) as T

    operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: String) {
        put(prop.name, value)
    }

    fun toJson(): JSONObject {
        Log.e("CustomDataClass", "toJson: ${commit()}")
        return JSONObject(commit())
    }

    override fun toString(): String {
        return super.toString()
    }

    /**
     * Cette methode permet de formatter et editer des champs automatiques
     * Il est fortement conseiller d'utiliser cette methode avant de sauvegarder
     */
    abstract fun commit(): Table

    open fun save(destination: CustomDataManager.Source, callback: ((Boolean, TableGen?) -> Unit)? = null) {
        CustomDataManager.save(destination, this) { b, mutableMap ->
            mutableMap?.let { if (b) this.putAll(it) }
            callback?.invoke(b, mutableMap)
        }
    }

    companion object {
        val CLASS = "className"
        val ID = "objectId"

        open fun getOriginalClassName(ref: String): String {
            return ref
        }


        fun create(map: TableGen): Pair<CustomDataClass?, Boolean> {
            if (!map.containsKey(CLASS)) return Pair<CustomDataClass?, Boolean>(null, false)
            return map[CLASS]?.toString()?.let {
                Pair<CustomDataClass?, Boolean>(
                    Class.forName(
                        getOriginalClassName(
                            it
                        )
                    ).kotlin.primaryConstructor?.call(map) as CustomDataClass, true
                )
            } ?: kotlin.run { Pair<CustomDataClass?, Boolean>(null, false) }
        }

        fun <T : CustomDataClass> create(map: TableGen, clazz: Class<T>): T? {
            avantSave(
                clazz
            )
            if (!map.containsKey(CLASS)) return null
            if (map.containsKey(CLASS) && getOriginalClassName(
                    map[CLASS].toString()
                ) != clazz.simpleName
            ) return null
            return map[CLASS]?.toString()?.let { clazz.kotlin.primaryConstructor?.call(map) }
                ?: kotlin.run { null }
        }

        fun <T : CustomDataClass> avantSave(clazz: Class<T>) {
            clazz.kotlin.declaredMemberProperties.forEach {
                //Log.e("CustomDataClass", "declaredMemberProperties: ${it.name} // ${it.returnType.javaType} // ${it.returnType.javaClass.isPrimitive} ")
            }
            //Log.e("CustomDataClass","declaredMemberProperties: ${clazz.kotlin.declaredMemberProperties} \n" + " declaredMemberExtensionProperties: ${clazz.kotlin.declaredMemberExtensionProperties}")
        }
    }

}