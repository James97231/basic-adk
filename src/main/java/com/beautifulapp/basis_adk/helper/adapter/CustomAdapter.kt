package com.beautifulapp.basis_adk.helper.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.full.primaryConstructor

class CustomAdapter<T, A : CustomAdapter.CustomViewHolder<T>>(
    private var mFragment: Fragment,
    private val resource: Int,
    private val clazz: Class<A>,
    click: ((View, Int, T) -> Unit)? = null,
    longClick: ((View, Int, T) -> Unit)? = null
) : RecyclerView.Adapter<A>() {
    val TAG = "CustomAdapter"
    var elements: MutableList<T> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var mclick = click
    var mLongClick = longClick


    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: A, position: Int) {
        holder.bindTo(elements[position], position, mFragment.viewLifecycleOwner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): A {
        return clazz.kotlin.primaryConstructor!!.call(LayoutInflater.from(parent.context).inflate(resource, parent, false)).apply {
            click = mclick
            longClick = mLongClick
            //onItemClickListener=mOnItemClickListener
        }
    }

    fun update(elements: MutableList<T>) {
        this.elements = elements
        //notifyDataSetChanged()
    }

    fun setClick(click: ((View, Int, T) -> Unit)?) {
        mclick = click
        notifyDataSetChanged()
    }

    fun setLongClick(longClick: ((View, Int, T) -> Unit)?) {
        mLongClick = longClick
        notifyDataSetChanged()
    }


    abstract class CustomViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        var click: ((View, Int, T) -> Unit)? = null
        var longClick: ((View, Int, T) -> Unit)? = null

        abstract fun bindTo(element: T, position: Int, lifecycleOwner: LifecycleOwner)
    }


}