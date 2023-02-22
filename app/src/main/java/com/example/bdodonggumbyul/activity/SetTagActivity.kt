package com.example.bdodonggumbyul.activity

import android.app.Activity
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
import com.example.bdodonggumbyul.MemoItem
import com.example.bdodonggumbyul.adapter.SetTagAdapter
import com.example.bdodonggumbyul.databinding.ActivitySetTagBinding
import com.example.bdodonggumbyul.dialog.AddBSDialog
import com.example.bdodonggumbyul.retrofit.RetrofitService
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetTagActivity : AppCompatActivity() {

    val binding: ActivitySetTagBinding by lazy { ActivitySetTagBinding.inflate(layoutInflater) }
    var tags = mutableListOf<String>()
    lateinit var tagAdapter: SetTagAdapter
    val rvm = FlexboxLayoutManager(this@SetTagActivity)

    //    var saveTags = mutableSetOf<String>()
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    val gson = GsonBuilder().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        pref = this@SetTagActivity.getSharedPreferences("userData", Context.MODE_PRIVATE)
        editor = pref.edit()
        tags = gson.fromJson(pref.getString("tags",""),tags::class.java)
        setRecycler()

        //done cancel 버튼 누르면 선택상태 저장하고 액티비티 종료
        binding.btnDone.setOnClickListener { doneEvent() }
        binding.btnDelete.setOnClickListener { deleteTag() }

        //et 작성후 submit하면 rv에 태그 추가
        binding.tagEt.setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> { //엔터가 두번 눌리는듯
                    if (event.action == KeyEvent.ACTION_DOWN) { //분기처리해서 중복입력 방지 //내가 해냄
                        editor.remove("tags")
                        var item = binding.tagEt.text.toString()
                        tags.add(item)
                        val tagArr = gson.toJson(tags)
                        editor.putString("tags", tagArr).commit()
                        tagAdapter.notifyItemInserted(tags.size)
                        binding.tagEt.text.clear()
                        Log.d("@@@@pref 데이터 추가 확인","${pref.getString("tags","")}")
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
        tagAdapter = SetTagAdapter(tags)
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
                        Log.d("@@@@선택한 태그 확인", "$isSelected")
                    }
                    true -> {
                        view.isSelected = false
                        isSelected.remove(tags[position])
                        Log.d("@@@@선택한 태그 확인", "$isSelected")
                    }
                }
            }
        })
    }

    fun deleteTag() {
        val dialog = AlertDialog.Builder(this@SetTagActivity)
            .setMessage("${isSelected} 태그를 삭제합니다.\n해당 태그를 포함한 메모들에서도 태그가 삭제됩니다.\n 계속하시겠습니까?")
            .setPositiveButton("네", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    tags.removeAll(isSelected)
                    tagAdapter.notifyDataSetChanged()
                    editor.remove("tags").commit()
                    editor.putString("tags",gson.toJson(tags)).commit()
                    Log.d("@@@@pref 데이터 삭제 확인","${pref.getString("tags","")}")
                    finish()
                }
            })
            .setNegativeButton("아니오", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
        dialog.show()
    }

    fun doneEvent() {
        editor.putString("selected_tag",gson.toJson(isSelected)).commit()
        Log.d("@@@@pref done 확인",isSelected.toString())
        val intent = Intent(this@SetTagActivity, MainActivity::class.java)
        intent.putExtra("tags", isSelected)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }
}