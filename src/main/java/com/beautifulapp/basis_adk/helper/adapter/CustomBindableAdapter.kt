package com.beautifulapp.basis_adk.helper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.full.primaryConstructor

class BindingCustomAdapter<T, A : BindingCustomAdapter.ViewHolder<T>, C : ViewDataBinding>(
    private var mFragment: Fragment,
    private val layoutId: Int,
    private val clazz: Class<A>
) : RecyclerView.Adapter<A>(),
    BindableAdapter<MutableList<T>?> {
    val TAG = "CustomAdapter"
    var elements: MutableList<T> = ArrayList()
        set(value) {
            val difference = value.size - value.count { field.contains(it) }
            val index = when (difference) {
                1 -> value.indexOf(value.first { !field.contains(it) })
                (-1) -> field.indexOf(field.first { !value.contains(it) })
                else -> -1
            }
            field = value
            when (difference) {
                1 -> notifyItemInserted(index)
                (-1) -> notifyItemRemoved(index)
                else -> notifyDataSetChanged()
            }

        }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: A, position: Int) {
        holder.bind(elements[position], position, mFragment.viewLifecycleOwner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): A {
        val binding = DataBindingUtil.inflate<C>(LayoutInflater.from(parent.context), layoutId, parent, false)
        val primary = clazz.kotlin.primaryConstructor!!
        return if (primary.parameters.size == 1) primary.call(binding) else primary.call(mFragment, binding)
    }

    override fun setData(data: MutableList<T>?) {
        data?.let {
            this.elements = it
        } ?: kotlin.run {
            this.elements = mutableListOf()
        }
        //notifyDataSetChanged()
    }

    abstract class ViewHolder<T>(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(element: T, position: Int, lifecycleOwner: LifecycleOwner)
    }
}

interface BindableAdapter<T> {
    fun setData(data: T)
}