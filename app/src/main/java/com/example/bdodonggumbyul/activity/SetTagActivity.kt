package com.example.bdodonggumbyul.activity

import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bdodonggumbyul.adapter.SetTagAdapter
import com.example.bdodonggumbyul.databinding.ActivitySetTagBinding
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class SetTagActivity : AppCompatActivity() {

    val binding: ActivitySetTagBinding by lazy { ActivitySetTagBinding.inflate(layoutInflater) }
    var tags = mutableListOf(
        "음식점", "개발공부", "게임"
    )
    val tagAdapter = SetTagAdapter(tags)
    val rvm = FlexboxLayoutManager(this@SetTagActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setRecycler()

        //done cancel 버튼 누르면 선택상태 저장하고 액티비티 종료
        binding.btnDone.setOnClickListener { doneEvent() }
        binding.btnCancel.setOnClickListener { cancelEvent() }


        //et 작성후 submit하면 rv에 태그 추가
        binding.tagEt.setOnKeyListener { _, keyCode, event ->
            when(keyCode){
                KeyEvent.KEYCODE_ENTER -> { //엔터가 두번 눌리는듯
                    if (event.action == KeyEvent.ACTION_DOWN){ //분기처리해서 중복입력 방지 //내가 해냄
                        tags.add(binding.tagEt.text.toString())
                        tagAdapter.notifyItemInserted(tags.size)
                        binding.tagEt.text.clear()
                    }
                    true
                }
                else -> {
                    Log.d("####error", keyCode.toString())
                    false
                }
            }
        }
        //rv내 태그 선택시 다중선택가능&색상 변화
    }

    var selected = mutableListOf<Boolean>()

    fun setRecycler() {
        binding.rvTag.adapter = tagAdapter
        rvm.justifyContent = JustifyContent.CENTER
        binding.rvTag.layoutManager = rvm

        for (i in 0..tags.size) selected.add(false)

        tagAdapter.setTagClickListener(object : SetTagAdapter.OnTagClickListener {
            override fun onClick(view: View, position: Int) {
                if (selected[position]){
                    selected[position] = !selected[position]
                    tagAdapter
                }else{
                    selected[position] = !selected[position]
                }
            }
        })
    }

    fun cancelEvent() {

//        val listener = DialogInterface.OnClickListener { dialog, which ->
//            dialog.dismiss()
//        }
//
//        AlertDialog.Builder(this@SetTagActivity)
//            .setMessage("태그 선택을 취소하시겠습니까?")
//            .setPositiveButton("네", listener)
//            .create()
        finish()
    }

    var cnt = 0

    fun doneEvent() {
        for(i in 0..selected.size){
            if (selected[i]) {
                cnt++

            }
        }

        Toast.makeText(this@SetTagActivity, ""+cnt, Toast.LENGTH_SHORT).show()
        //레트로핏
        finish()
    }
}