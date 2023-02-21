package com.example.bdodonggumbyul.activity

import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.bdodonggumbyul.dialog.AddBSDialog
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class SetTagActivity : AppCompatActivity() {

    val binding: ActivitySetTagBinding by lazy { ActivitySetTagBinding.inflate(layoutInflater) }
    var tags = mutableListOf<String>()
    lateinit var tagAdapter : SetTagAdapter
    val rvm = FlexboxLayoutManager(this@SetTagActivity)
//    var saveTags = mutableSetOf<String>()
    lateinit var pref: SharedPreferences
    lateinit var editor :SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        pref = this@SetTagActivity.getSharedPreferences("userData", Context.MODE_PRIVATE)
        editor = pref.edit()

        setRecycler()

        //done cancel 버튼 누르면 선택상태 저장하고 액티비티 종료
        binding.btnDone.setOnClickListener { doneEvent() }
        binding.btnDelete.setOnClickListener { deleteTag() }


        //et 작성후 submit하면 rv에 태그 추가
        binding.tagEt.setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> { //엔터가 두번 눌리는듯
                    if (event.action == KeyEvent.ACTION_DOWN) { //분기처리해서 중복입력 방지 //내가 해냄
                        var item = binding.tagEt.text.toString()
                        tags.add(item)
                        editor.putString("tags",item.toHashSet()).commit()
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

    var isSelected = arrayListOf<String>()
    fun setRecycler() {
        tagAdapter = SetTagAdapter(saveTags.toMutableList())
        binding.rvTag.adapter = tagAdapter
        rvm.justifyContent = JustifyContent.CENTER
        binding.rvTag.layoutManager = rvm

        tagAdapter.setTagClickListener(object : SetTagAdapter.OnTagClickListener {
            override fun onClick(view: View, position: Int) {
                Toast.makeText(this@SetTagActivity, "" + tags[position], Toast.LENGTH_SHORT).show()
                when (isSelected.contains(tags[position])) {
                    false -> {
                        isSelected.add(tags[position])
                        view.isSelected = true
                        Log.d("@@@@선택한 태그 확인","$isSelected")
                    }
                    true -> {
                        view.isSelected = false
                        isSelected.remove(tags[position])
                        Log.d("@@@@선택한 태그 확인","$isSelected")
                    }
                }
            }
        })
    }

    fun deleteTag() {
        val dialog = AlertDialog.Builder(this@SetTagActivity).setMessage("${isSelected} 태그를 삭제합니다. 계속하시겠습니까?")
            .setPositiveButton("네",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    tags.removeAll(isSelected)
                    tagAdapter.notifyDataSetChanged()
                    saveTags.
                    finish()
                }
            })
        dialog.setNegativeButton("아니오", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        dialog.show()
    }

    fun doneEvent() {
        val intent = Intent(this@SetTagActivity, AddBSDialog::class.java)
        intent.putExtra("tags", isSelected)
        startActivity(intent)
        finish()
    }
}