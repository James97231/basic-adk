package com.beautifulapp.basis_adk.helper.pattern

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment


abstract class CustomViewModel(val app: Application) : AndroidViewModel(app) {
    var isSetup = false
    val loading = MutableLiveData<Boolean>()
    var onBackPressed: (() -> Unit)? = null
    var startActivityForResult: ((intent: Intent?, requestCode: Int, options: Bundle?) -> Unit)? = null
    private var _goTo: ((it: View, actionId: Int, bundle: Bundle?, navOptions: NavOptions?, navExtras: Navigator.Extras?) -> Unit) =
        { it, actionId, bundle, navOptions, navExtras ->
            Navigation.findNavController(it).navigate(actionId, bundle, navOptions, navExtras)
        }
    var sendDataToLastFragment: ((data: TableGen?) -> Unit)? = null
    private var requestPermissions: ((permissions: Array<String>, requestCode: Int) -> Unit)? = null

    open fun onArgumentsReceived(params: HashMap<String, Any?>) {}


    fun goTo(it: View, actionId: Int, bundle: Bundle? = null, navOptions: NavOptions? = null, navExtras: Navigator.Extras? = null) =
        _goTo.invoke(it, actionId, bundle, navOptions, navExtras)


    /**
     * Cette methode permet de lier le [CustomFragment] et le [CustomViewModel] et permet ainsi acceder à des methodes et listeners propres Fragment dans le ViewModel
     * liste des methodes et listerners:
     * - onActivityResult
     * - onRequestPermissionsResult
     * - onBackPressed
     * - startActivityForResult
     * - requestPermissions
     * - onExternalDataReceive : listener sur des dannées echangées entre fragment successive (appelant <- appelé )
     * - sendDataToLastFragment
     */
    open fun setup(fragment: Fragment) {
        setArguments(fragment.arguments)
        onBackPressed = { fragment.activity?.onBackPressed() }
        startActivityForResult = { intent, requestCode, options ->
            fragment.startActivityForResult(
                intent,
                requestCode,
                options
            )
        }
        requestPermissions =
            { permissions, requestCode -> fragment.requestPermissions(permissions, requestCode) }
        sendDataToLastFragment = { data ->
            NavHostFragment.findNavController(fragment).previousBackStackEntry?.savedStateHandle?.set(
                DATA_ID, data
            )
        }

        NavHostFragment.findNavController(fragment).currentBackStackEntry?.savedStateHandle?.getLiveData<TableGen>(
            DATA_ID
        )?.observe(fragment.viewLifecycleOwner, Observer {
            onExternalDataReceive(it)
        })

        _goTo = { view, actionId, bundle, navOptions, navExtras ->
            com.beautifulapp.basis_adk.goTo(
                view,
                actionId,
                bundle,
                navOptions,
                navExtras,
                fragment.activity
            )
        }

        isSetup = true
    }

    fun setArguments(bundle: Bundle?) {
        HashMap<String, Any?>().apply {
            bundle?.keySet()?.forEach { this[it] = bundle.get(it) }
            onArgumentsReceived(this)
        }
    }

    fun requestPerms(perms: Array<String>, resquestCode: Int = 1, action: (() -> Unit)?) {
        app.let {
            perms.apply {
                if (any { perm -> ActivityCompat.checkSelfPermission(it, perm) != PackageManager.PERMISSION_GRANTED }) {
                    requestPermissions?.invoke(this@apply, resquestCode)
                } else {
                    Log.e("requestPerms", "action.invoke()")
                    action?.invoke()
                }
            }
        }
    }

    open fun save(data: TableGen?, callback: (() -> Unit)? = null) {}

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    open fun onRequestPermissionsResult(RequestCode: Int, permissions: Array<out String>, grantedResults: IntArray) {}

    open fun onExternalDataReceive(data: TableGen?) {}

    override fun onCleared() {
        super.onCleared()
        Log.e(javaClass.name, " clife onCleared()")
    }

    init {
        Log.e(javaClass.name, " clife init")
    }

    companion object {
        const val MAP = "map"
        const val ID = "id"
        const val RUNTIME = "runtime"
        const val ITEM = "item"
        const val DATA_ID = "data"
    }


}