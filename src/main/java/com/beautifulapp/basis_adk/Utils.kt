package com.beautifulapp.basis_adk

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StyleRes
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun sendBroadcast(intent: Intent, m_LocalBroadcastManager: LocalBroadcastManager?, m_Context: Context) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        val local = m_LocalBroadcastManager ?: LocalBroadcastManager.getInstance(m_Context)
        local.sendBroadcastSync(intent)
    } else
        m_Context.sendBroadcast(intent)
}

fun registerReceiver(broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter, m_Context: Context) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        LocalBroadcastManager.getInstance(m_Context).registerReceiver(broadcastReceiver, intentFilter)
    } else
        m_Context.registerReceiver(broadcastReceiver, intentFilter)
}

fun unregisterReceiver(broadcastReceiver: BroadcastReceiver, m_Context: Context) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        LocalBroadcastManager.getInstance(m_Context).unregisterReceiver(broadcastReceiver)
    } else
        m_Context.unregisterReceiver(broadcastReceiver)
}

fun saveBitmap(bitmap: Bitmap?, path: String, format: Bitmap.CompressFormat? = Bitmap.CompressFormat.PNG): Boolean {
    if (bitmap == null) return false
    var result = false
    try {
        File(path).apply {
            if (!exists()) parentFile.mkdirs()
        }

        FileOutputStream(path).apply {
            bitmap.compress(format, 60, this)
            flush()
            close()
        }
        result = true
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return result
}

fun getDate(date: String, format: String = "EEE MMM dd HH:mm:ss zzz yyyy"): Date =
    try {
        SimpleDateFormat(format, Locale.US).parse(date)
    } catch (e: ParseException) {
        Date()
    }

fun getTime(milliSeconds: Long, format: String = "HH:mm:ss.SS"): String =
    SimpleDateFormat(format).apply { /*timeZone = TimeZone.getTimeZone("GMT")*/ }.format(Calendar.getInstance().apply { timeInMillis = milliSeconds }.time)
//SimpleDateFormat(format).format(Date(milliSeconds).time)

fun checkText(edit: EditText, test: Boolean, context: Context): Boolean {
    return if (test) {
        edit.requestFocus()
        edit.error = "Champs manquant "
        edit.startAnimation(AnimationUtils.loadAnimation(context, R.anim.small_shake).apply { repeatCount = 3 })
        false
    } else {
        edit.error = null
        true
    }
}

fun checkView(v: View, test: Boolean, context: Context): Boolean {
    return if (test) {
        v.requestFocus()
        v.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.small_shake).apply { repeatCount = 3 })
        Toast.makeText(
            context,
            "Une action est nécessaire sur la vue qui vibre",
            Toast.LENGTH_SHORT
        ).apply {
            //setGravity(Gravity.TOP,0,(v.y+v.height+5).toInt())
        }.show()
        false
    } else {
        true
    }
}

fun goTo(it: View, actionId: Int, bundle: Bundle? = null, navOptions: NavOptions? = null, navExtras: Navigator.Extras? = null, activity: Activity? = null) {
    (activity as? Pub)?.apply {
        if (adIsLoaded && Math.random() <= this.frequency) {
            //showAd { Navigation.findNavController(it).navigate(actionId, bundle, navOptions, navExtras) }
            Navigation.findNavController(it).navigate(actionId, bundle, navOptions, navExtras)
            showAd { }
        } else Navigation.findNavController(it).navigate(actionId, bundle, navOptions, navExtras)
    } ?: kotlin.run {
        Navigation.findNavController(it).navigate(actionId, bundle, navOptions, navExtras)
    }
}


fun navigate(it: View, actionId: Int, activity: Pub?, bundle: Bundle? = null) {
    activity?.apply {
        if (adIsLoaded && Math.random() >= 0.5)
            showAd { Navigation.findNavController(it).navigate(actionId, bundle) }
        else Navigation.findNavController(it).navigate(actionId, bundle)
    } ?: kotlin.run {
        Navigation.findNavController(it).navigate(actionId, bundle)
    }
}

fun FromDeepLink(bundle: Bundle?) = bundle?.containsKey("android-support-nav:controller:deepLinkIntent") == true


fun blur(image: Bitmap?, blur_radius: Int, sizeratio: Int, context: Context): Bitmap? {
    var result: Bitmap? = null
    if (null == image) return result
    val tps = System.currentTimeMillis()
    result = Bitmap.createScaledBitmap(image, image.width / sizeratio, image.height / sizeratio, false)
    //result = Bitmap.createScaledBitmap(image, 250, 250, false);
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, result, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(blur_radius /* e.g. 3.f */.toFloat())
    script.setInput(input)
    script.forEach(output)
    output.copyTo(result)
    println("mEndIndex blurring: " + (System.currentTimeMillis() - tps) + " ms")
    return result
}

fun takePictureIntent(c: Context, extension: String? = "jpeg"): Pair<Intent, Uri?> {
    val uri = FileProvider.getUriForFile(
        c,
        "${c.packageName}.provider",
        createTempFile(suffix = extension, directory = c.externalCacheDir)
    )
    val intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
    return Pair(intent, uri)
}

fun pickFileIntent(type: String? = "image/*"): Pair<Intent, Uri?> {
    return Pair(Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE);this.type = type
    }, null)
}

/**
 * It's methode shown a alert dialog to select take or select picture, by default use externalcacheDir like directory to take photo
 * @param c [Context]
 * @param themeId apply this style to apply at ther alert dialog
 * @param callback [KFunction3] where intent is the permit to take or select picture,
 * uri is null if select picture or specific where picture is saved,
 * the last parameter is a function that permit to convert uri to original file path
 *
 */
fun imagePicker(c: Context, @StyleRes themeId: Int, callback: (Intent, Uri?) -> Unit) {
    MaterialAlertDialogBuilder(c, themeId)
        .setItems(R.array.picture_choices) { dialogInterface, i ->
            val (r1, r2) = when (i) {
                0 -> takePictureIntent(c)
                else -> pickFileIntent()
            }
            callback(r1, r2)
            dialogInterface.dismiss()
        }
        .show()
}

/**
 * It's methode shown a alert dialog to select take or select picture, by default use externalcacheDir like directory to take photo
 * @param c [Context]
 * @param themeId apply this style to apply at ther alert dialog
 * @param callback [KFunction3] where intent is the permit to take or select picture,
 * uri is null if select picture or specific where picture is saved,
 * the last parameter is a function that permit to convert uri to original file path
 *
 */
fun filePicker(c: Context, @StyleRes themeId: Int, callback: (Intent, Uri?) -> Unit) {
    MaterialAlertDialogBuilder(c, themeId)
        .setItems(R.array.file_choices) { dialogInterface, i ->
            val (r1, r2) = when (i) {
                0 -> takePictureIntent(c)
                1 -> pickFileIntent("image/*")
                else -> pickFileIntent("application/pdf")
            }
            callback(r1, r2)
            dialogInterface.dismiss()
        }
        .show()
}


fun imageRotationFromUri(context: Context, uri: Uri) = context.contentResolver.openInputStream(uri)
    ?.let { ExifInterface(it).rotationDegrees.apply { it.close() } } ?: 0

/**
 * Renvoie un image representant la premere page du fichier PDF
 */
fun getPdfPreview(
    data: ByteArray,
    directory: String?,
    callback: ((Bitmap) -> Unit)? = null
): Bitmap {
    val fileCopy = File(directory, "myPdf") //anything as the name

    fileCopy.parentFile?.mkdirs()
    fileCopy.writeBytes(data)

    val fileDescriptor = ParcelFileDescriptor.open(fileCopy, ParcelFileDescriptor.MODE_READ_ONLY)
    val mPdfRenderer = PdfRenderer(fileDescriptor)
    val mPdfPage = mPdfRenderer.openPage(0)

    val bitmap = Bitmap.createBitmap(
        mPdfPage.width,
        mPdfPage.height,
        Bitmap.Config.ARGB_8888
    ) //Not RGB, but ARGB
    mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
    mPdfPage.close()
    mPdfRenderer.close()
    fileCopy.delete()
    callback?.invoke(bitmap)
    return bitmap
}

fun showDatePicker(v: View, d: MutableLiveData<Date>) {
    val c =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { d.value?.let { this.time = it } }
    DatePickerDialog(
        v.context,
        DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
            val date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(year, monthOfYear, dayOfMonth, 0, 0, 0);set(
                Calendar.MILLISECOND,
                0
            )
            }.time
            d.setValue(date)
        },
        c.get(Calendar.YEAR),
        c[Calendar.MONTH],
        c[Calendar.DATE]
    ).show()
}


fun showTimePicker(v: View, t: MutableLiveData<Number>) {
    TimePickerDialog(
        v.context,
        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            t.value = hourOfDay * 3600 + minute * 60
        },
        t.value?.toInt()?.div(3600) ?: 0,
        t.value?.toInt()?.rem(3600)?.div(60) ?: 0,
        true
    ).show()
}


