package com.example.bdodonggumbyul.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bdodonggumbyul.MemoItem
//import com.example.bdodonggumbyul.databinding.ItemHomeDateBinding

//class MainDateRecyclerAdapter(var context: Context, var list: MutableList<MemoItem>): RecyclerView.Adapter<MainDateRecyclerAdapter.VH>() {

//    lateinit var binding: ItemHomeDateBinding
//    val adapter = MainRecyclerAdapter(context, list)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        binding = ItemHomeDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return VH(binding.root)
//    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        val item = list[position]
//        holder.bind(item)
//    }
//
//    inner class VH(itemview: View): ViewHolder(itemview) {
//
//        val binding = ItemHomeDateBinding.bind(itemview)
//
//        fun bind(memo: MemoItem){
//            binding.timeDate.text = memo.date
//            binding.rvContent.adapter
//        }
//    }
//}