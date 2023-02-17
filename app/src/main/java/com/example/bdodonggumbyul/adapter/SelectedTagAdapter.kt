package com.example.bdodonggumbyul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bdodonggumbyul.databinding.ItemTagBinding

class SelectedTagAdapter(var tags: MutableList<String>): RecyclerView.Adapter<SelectedTagAdapter.VH>() {

    lateinit var binding: ItemTagBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return VH(binding.root)
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(tags[position])
    }

    inner class VH(itemview: View): ViewHolder(itemview){
        val binding = ItemTagBinding.bind(itemview)

        fun bind(tag: String){
            binding.tvTag.text = tag
        }
    }
}