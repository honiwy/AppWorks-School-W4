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
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.marsrealestate.R
import com.example.android.marsrealestate.databinding.GridViewItemBinding
import com.example.android.marsrealestate.databinding.ListPropertyBinding
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 * @param onClick a lambda that takes the
 */
private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1
class PhotoGridAdapter( val onClickListener: OnClickListener ) :
        ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    //如果input List<MarsProperty>是null就只放Header,如果不是就放header+List<MarsProperty>//在overviewFragment用到
    fun addHeaderAndSubmitList(list: List<MarsProperty>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.MarsPropertyItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    //收納MarsPropertyView之處,連到ListProperty.xml,產出一個個要顯示的MarsProperty組塊
    class MarsPropertyViewHolder private constructor(val binding: ListPropertyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(marsProperty: MarsProperty,clickListener: OnClickListener) {
            binding.property = marsProperty
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): MarsPropertyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListPropertyBinding.inflate(layoutInflater, parent, false)
                return MarsPropertyViewHolder(binding)
            }
        }
    }
    //收納TextView之處,連到Header.xml,產出一個要顯示的header組塊
    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    //如果是DataItem.Header則viewType是0;如果是DataItem.MarsPropertyItem則viewType是1
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.MarsPropertyItem -> ITEM_VIEW_TYPE_ITEM
        }
    }
    //如果viewType是0則顯示header組塊;viewType是1則顯示MarsProperty組塊
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> MarsPropertyViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }
    //如果input是MarsPropertyViewHolder就為它裝上binding的property和listener來更新
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MarsPropertyViewHolder -> {
                val marsPropertyItem = getItem(position) as DataItem.MarsPropertyItem
                holder.bind(marsPropertyItem.marsProperty,onClickListener)
            }
        }
    }
}
//判斷是否為同一物件,是否該recycle了
class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem === newItem
    }
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }
}
//?
class OnClickListener(val clickListener: (marsPropertyId:String) -> Unit) {
    fun onClick(marsProperty:MarsProperty) = clickListener(marsProperty.id)
}
//?
sealed class DataItem {
    data class MarsPropertyItem(val marsProperty: MarsProperty): DataItem() {
        override val id = marsProperty.id
        override val property = marsProperty
    }

    object Header: DataItem() {
        override val id = "header"
        override val property = null
    }

    abstract val id: String
    abstract val property:MarsProperty?
}
