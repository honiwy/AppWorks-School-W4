/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.marsrealestate.overview

import android.view.LayoutInflater
import android.view.View
import com.example.android.marsrealestate.network.MarsProperty

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.marsrealestate.R
import com.example.android.marsrealestate.databinding.GridViewItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class PhotoGridAdapter( val clickListener: PhotoGridListener ) :
        ListAdapter<DataItem, RecyclerView.ViewHolder>(PhotoGridDiffCallback) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addHeaderAndSubmitList(list: List<MarsProperty>?) {
        adapterScope.launch {
            val items = when (list){
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.MarsItem(it) }
            }
            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,  position: Int) {
        when (holder) {
            is MarsPropertyViewHolder -> {
                val marsItem = getItem(position) as MarsProperty
                holder.itemView.setOnClickListener {
                   clickListener.onClick(marsItem)}
                holder.bind(marsItem)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> MarsPropertyViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context)))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }
    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }
    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.MarsItem -> ITEM_VIEW_TYPE_ITEM
        }
    }


    class MarsPropertyViewHolder(private var binding: GridViewItemBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(marsProperty: MarsProperty) {
            binding.property = marsProperty
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    companion object PhotoGridDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class PhotoGridListener(val clickListener: (marsProperty: MarsProperty) -> Unit) {
        fun onClick(marsProperty:MarsProperty) = clickListener(marsProperty)
    }


}
sealed class DataItem{
    data class MarsItem(val mars: MarsProperty):DataItem(){
        override val id = mars.id.toLong()
    }
    object  Header:DataItem(){
        override val id = Long.MIN_VALUE
    }

    abstract val id:Long
}