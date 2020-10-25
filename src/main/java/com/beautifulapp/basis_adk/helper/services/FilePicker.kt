package com.beautifulapp.basis_adk.helper.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.annotation.StyleRes
import androidx.lifecycle.MediatorLiveData
import com.beautifulapp.basis_adk.filePicker
import com.beautifulapp.basis_adk.helper.pattern.CustomViewModel
import com.beautifulapp.basis_adk.imagePicker
import kotlin.random.Random

class FilePicker(c: Context?, vm: CustomViewModel, rqC: Int, @StyleRes styleId: Int) {
    private val requestTakePhoto = Random.nextInt(0, 1024)
    private val requestPickWithExplorer = Random.nextInt(0, 1024)
    private var mRequestPerms: (() -> Unit)? = null
    private var photoUri: Uri? = null
    var fileUri = MediatorLiveData<Uri>()

    val onRequestPermissionsResultAction =
        { requestCode: Int -> if (rqC == requestCode) mRequestPerms?.invoke() }
    val onActivityResultAction = { requestCode: Int, resultCode: Int, data: Intent? ->
        if (requestCode == requestTakePhoto && resultCode == Activity.RESULT_OK) {
            photoUri?.let { fileUri.value = it }
            //fileUri.value = fileUri.value
        }
        if (requestCode == requestPickWithExplorer && resultCode == Activity.RESULT_OK) fileUri.postValue(
            data?.data
        )
    }

    val show = { v: View? ->
        filePicker(v?.context ?: vm.app, styleId) { intent, uri ->
            val taken = uri != null
            mRequestPerms = {
                vm.requestPerms(if (taken) CAM_PERMS else WRITE_PERMS, rqC) {
                    vm.startActivityForResult?.invoke(
                        intent,
                        uri?.let { requestTakePhoto } ?: requestPickWithExplorer,
                        null)
                    photoUri = uri
                }
            }.apply { invoke() }
        }
    }

    val showImagePicker = { v: View? ->
        imagePicker(v?.context ?: vm.app, styleId) { intent, uri ->
            val taken = uri != null
            mRequestPerms = {
                vm.requestPerms(if (taken) CAM_PERMS else WRITE_PERMS, rqC) {
                    vm.startActivityForResult?.invoke(
                        intent,
                        uri?.let { requestTakePhoto } ?: requestPickWithExplorer,
                        null)
                    photoUri = uri
                }
            }.apply { invoke() }
        }
    }


    companion object {
        private val CAM_PERMS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private val WRITE_PERMS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

}