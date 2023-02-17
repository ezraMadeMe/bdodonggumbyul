package com.example.bdodonggumbyul.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bdodonggumbyul.Memo
import com.example.bdodonggumbyul.databinding.ItemHomeBinding

class MainRecyclerAdapter(var list: MutableList<Memo>): RecyclerView.Adapter<MainRecyclerAdapter.VH>() {

    interface OnClickListener{ fun onClick(view: View, position: Int) }
    fun setItemClickListener(onItemClickListener: OnClickListener){
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener: OnClickListener

    lateinit var binding: ItemHomeBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding.root)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener { itemClickListener.onClick(it,position) }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemHomeBinding.bind(itemView)

        fun bind(memo: Memo) {
            binding.timeStamp.text = memo.timestamp
            binding.textDetail.text = memo.memo
        }
    }
}