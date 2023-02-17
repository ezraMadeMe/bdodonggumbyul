package com.example.bdodonggumbyul.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bdodonggumbyul.Memo
import com.example.bdodonggumbyul.adapter.MainRecyclerAdapter
import com.example.bdodonggumbyul.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    var items: MutableList<Memo> = mutableListOf(
        Memo("2023/12/23","13:04","testestesteseetes")
    )
    lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}