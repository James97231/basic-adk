package com.beautifulapp.basis_adk.helper

import android.R
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.beautifulapp.basis_adk.helper.Converter.dateToString
import com.beautifulapp.basis_adk.helper.Converter.secondToString
import com.beautifulapp.basis_adk.helper.adapter.BindableAdapter
import com.beautifulapp.basis_adk.helper.services.BitmapDrawableCache
import com.beautifulapp.basis_adk.showDatePicker
import com.beautifulapp.basis_adk.showTimePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.system.measureTimeMillis


@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}


@BindingAdapter(value = ["imageBD", "default"], requireAll = false)
fun bindImage(view: ImageView, imageUrl: String?, def: Drawable?) {
    if (!imageUrl.isNullOrEmpty()) {
        BitmapDrawableCache[imageUrl]?.value?.let {
            view.setImageDrawable(it)
        } ?: run {
            GlobalScope.launch {
                BitmapDrawableCache.bitmapFromAll(view.context, imageUrl)?.apply {
                    BitmapDrawableCache.addFromBitmapDrawable(imageUrl, this)
                    withContext(Dispatchers.Main) {
                        view.setImageDrawableWithAnimation(this@apply, 500)
                    }
                }
            }
        }
    } else def.let { view.setImageDrawable(it) }
}

@BindingAdapter("data")
fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, data: T) {
    if (recyclerView.adapter is BindableAdapter<*>) {
        (recyclerView.adapter as BindableAdapter<T>).setData(data)
    }
}

@get:InverseBindingAdapter(attribute = "value", event = "valueAttrChanged")
@set:BindingAdapter("value")
var AutoCompleteTextView.selectedValue: Any?
    get() = (adapter as? ArrayAdapter<Any?>)?.getPosition(text.toString())
        ?.let { adapter.getItem(it) }
    set(value) {
        value?.let {
            (adapter as? ArrayAdapter<Any?>)?.let { ad ->
                Log.e(
                    "BindingAdapters",
                    "set selectedValue changevalue: $listSelection   ${ad.getPosition(it)}   ${(adapter as? ArrayAdapter<Any?>)?.getPosition(
                        value
                    )}"
                )
            }
            setText(it.toString(), false)
        }
    }

@get:InverseBindingAdapter(attribute = "position", event = "valueAttrChanged")
@set:BindingAdapter("position")
var AutoCompleteTextView.selectedPosition: Int
    get() = (adapter as? ArrayAdapter<Any?>)?.getPosition(text.toString()) ?: -1
    set(value) {
        Log.e(
            "BindingAdapters",
            "set selectedValue valid: $listSelection   ${value}   ${(adapter as? ArrayAdapter<Any?>)?.getPosition(
                value
            )}"
        )
        setText((adapter as? ArrayAdapter<Any?>)?.getItem(value).toString(), false)
    }


@BindingAdapter("entries", "itemLayout", requireAll = false)
fun AutoCompleteTextView.bindAdapter(entries: Array<Any?>, @LayoutRes itemLayout: Int?) {
    setAdapter(ArrayAdapter(context, itemLayout ?: R.layout.simple_dropdown_item_1line, entries))
}

/**
 * use to Edit keyboard action
 */
@BindingAdapter("onEditorEnterAction")
fun EditText.onEditorEnterAction(action: ((View?) -> Unit)?) {

    if (action == null) setOnEditorActionListener(null)
    else setOnEditorActionListener { v, actionId, event ->

        val imeAction = when (actionId) {
            EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_GO -> true
            else -> false
        }

        val keydownEvent =
            event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

        if (imeAction || keydownEvent)
            true.also { action.invoke(this) }
        else false
    }
}

@BindingAdapter("focus")
fun View.focus(b: Boolean) {
    if (b) requestFocus()
    else clearFocus()
}


/**
 * Use EditText like datePicker
 */
@BindingAdapter("date", "dateFormat", requireAll = false)
fun TextInputEditText.date(date: MutableLiveData<Date>, dateFormat: String?) {
    setText(dateToString(date.value, dateFormat ?: "d MMM yyyy"))
    error = null
    if (!hasOnClickListeners()) {
        keyListener = null
        setOnClickListener { showDatePicker(it, date) }
        setOnFocusChangeListener { v, hasFocus -> if (hasFocus) showDatePicker(v, date) }
    }
}

/**
 * Use EditText like timePicker
 */
@BindingAdapter("time", "timeFormat", requireAll = false)
fun TextInputEditText.time(time: MutableLiveData<Number>, timeFormat: String?) {
    setText(secondToString(time.value, timeFormat ?: "H'h'mm"))
    error = null
    if (!hasOnClickListeners()) {
        keyListener = null
        setOnClickListener { showTimePicker(it, time) }
        setOnFocusChangeListener { v, hasFocus -> if (hasFocus) showTimePicker(v, time) }
    }
}

@BindingAdapter("enum", "itemLayout", requireAll = false)
fun AutoCompleteTextView.setEnum(enum: Array<Any?>, @LayoutRes itemLayout: Int?) {
    measureTimeMillis {
        setOnClickListener { showDropDown() }
        setOnFocusChangeListener { v, hasFocus -> if (hasFocus) showDropDown() }
        setAdapter(ArrayAdapter(context, itemLayout ?: R.layout.simple_dropdown_item_1line, enum))
    }
}
