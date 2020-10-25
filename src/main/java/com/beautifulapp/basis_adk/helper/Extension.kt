package com.beautifulapp.basis_adk.helper

import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import com.beautifulapp.basis_adk.R

fun ImageView.setImageDrawableWithAnimation(drawable: Drawable, duration: Int = 300) {
    val currentDrawable = getDrawable()
    if (currentDrawable == null) {
        setImageDrawable(drawable)
        return
    }

    val transitionDrawable = TransitionDrawable(arrayOf(currentDrawable, drawable))
    setImageDrawable(transitionDrawable)
    transitionDrawable.startTransition(duration)
}

fun EditText.displayError(errorMessage: String? = null) {
    requestFocus()
    error = errorMessage
    startAnimation(
        AnimationUtils.loadAnimation(context, R.anim.small_shake).apply { repeatCount = 3 })
}

fun EditText.displayError(@StringRes errorMessage: Int? = null) {
    displayError(this.resources.getString(errorMessage ?: R.string.invalid_field))
}

fun View.displayError(errorMessage: String? = null) {
    requestFocus()
    startAnimation(
        AnimationUtils.loadAnimation(context, R.anim.small_shake).apply { repeatCount = 3 })
    Toast.makeText(
        context,
        errorMessage ?: "Une action est nécessaire sur la vue qui vibre",
        Toast.LENGTH_SHORT
    ).apply {
        //setGravity(Gravity.TOP,0,(v.y+v.height+5).toInt())
    }.show()
}

fun View.displayError(@StringRes errorMessage: Int? = null) {
    displayError(this.resources.getString(errorMessage ?: R.string.action_required))
}

fun View.toTransitionGroup() = this to transitionName

/*fun Fragment.waitForTransition(targetView: View) {
    postponeEnterTransition()
    targetView.doOnPreDraw { startPostponedEnterTransition() }
}*/