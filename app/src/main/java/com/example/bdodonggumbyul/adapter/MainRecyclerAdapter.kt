package com.example.bdodonggumbyul.adapter

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.databinding.ItemHomeBinding
import com.google.gson.GsonBuilder

class MainRecyclerAdapter(var context: Context, var list: MutableList<MemoItem>) :
    RecyclerView.Adapter<MainRecyclerAdapter.VH>() {
    interface OnClickListener { fun onClick(view: View, position: Int) }
    fun setItemClickListener(onItemClickListener: OnClickListener) { this.itemClickListener = onItemClickListener }
    private lateinit var itemClickListener: OnClickListener

    interface OnLongClickListener { fun onLongClick(view: View, position: Int) : Boolean }
    fun setItemLongClickListener(onItemLongClickListener: OnLongClickListener) { this.itemLongClickListener = onItemLongClickListener }
    private lateinit var itemLongClickListener: OnLongClickListener

    lateinit var binding: ItemHomeBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding.root)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { itemClickListener.onClick(it, position) }
        holder.itemView.setOnLongClickListener { itemLongClickListener.onLongClick(it, position) }

        //이미지가 없는 아이템에도 이미지가 붙는 문제 //분기처리 이렇게 하니 해결된듯?
        if (item.image.equals("")) {
            holder.binding.itemIv.visibility = View.GONE
        } else {
            holder.binding.itemIv.visibility = View.VISIBLE
            val imgUrl = "http://ezra2022.dothome.co.kr/memo/${item.image}"
            Glide.with(context).load(imgUrl).into(holder.binding.itemIv)
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemHomeBinding.bind(itemView)
        fun bind(memo: MemoItem) { //같은 날짜가 연속될 경우 날짜 tv가 안 보이게 하고싶운뎅..
            binding.timeDate.text = memo.date
            binding.timeStamp.text = memo.timestamp
            binding.textDetail.text = memo.content
            binding.timeTag.text = when(memo.tag) {
                null -> ""
                else ->" #${memo.tag}"
            }
        }
    }
}