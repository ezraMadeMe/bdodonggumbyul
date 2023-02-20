package com.example.bdodonggumbyul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bdodonggumbyul.databinding.ItemDailyCalendarBinding

class VCalendarAdapter(var list: MutableList<String>): RecyclerView.Adapter<VCalendarAdapter.VH>() {

    //날짜 가져오는 라이브러리
    //
    interface OnClickListener {  fun onClick(view: View, position: Int) }
    fun setItemClickListener(onItemClickListener: OnClickListener) {
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener : OnClickListener

    lateinit var binding: ItemDailyCalendarBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = ItemDailyCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding.root)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener { itemClickListener.onClick(it,position) }
    }

    inner class VH(itemview: View): ViewHolder(itemview) {
        val binding = ItemDailyCalendarBinding.bind(itemview)

        fun bind(list: String){
            binding.vDay.text = list
        }
    }
}