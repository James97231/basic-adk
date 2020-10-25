package com.beautifulapp.basis_adk.helper.pattern

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController


open class CustomFragment : Fragment() {

    private var _viewModel: CustomViewModel? = null

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
    open fun setupViewModel(vm: CustomViewModel? = null) {
        _viewModel = vm
        _viewModel?.setup(this)
    }

    fun sendDataToLastFragment(data: TableGen? = null) {
        findNavController(this).previousBackStackEntry?.savedStateHandle?.set(CustomViewModel.DATA_ID, data)
    }

    open fun onDataReceiveFromLastFragment(table: Table) {
        _viewModel?.onExternalDataReceive(table)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        _viewModel?.onActivityResult(requestCode, resultCode, data)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        _viewModel?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        Log.e("OnResume", javaClass.simpleName)
        super.onResume()
    }

    override fun onPause() {
        Log.e("onPause", javaClass.simpleName)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("onCreate", javaClass.simpleName)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("onCreateView", javaClass.simpleName)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        Log.e("onDestroyView", javaClass.simpleName)
        super.onDestroyView()
    }

    override fun onStop() {
        Log.e("onStop", javaClass.simpleName)
        super.onStop()
    }

}