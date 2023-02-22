package com.example.bdodonggumbyul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bdodonggumbyul.databinding.ItemFilterTagBinding
import com.example.bdodonggumbyul.databinding.ItemTagBinding

class SelectedTagAdapter(var tags: ArrayList<String>): RecyclerView.Adapter<SelectedTagAdapter.VH>() {

    interface OnFilterTagClickListener { fun onClick(view: View, posision: Int) }

    fun setFilterTagClickListener(onFilterTagClickListener: OnFilterTagClickListener){
        this.filterTagClickListener = onFilterTagClickListener
    }
    private lateinit var filterTagClickListener: OnFilterTagClickListener

    lateinit var binding: ItemFilterTagBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = ItemFilterTagBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return VH(binding.root)
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(tags[position])
        holder.itemView.setOnClickListener { filterTagClickListener.onClick(it, position) }
    }

    inner class VH(itemview: View): ViewHolder(itemview){
        val binding = ItemFilterTagBinding.bind(itemview)

        fun bind(tag: String){
            binding.tvTagName.text = tag
        }
    }
}