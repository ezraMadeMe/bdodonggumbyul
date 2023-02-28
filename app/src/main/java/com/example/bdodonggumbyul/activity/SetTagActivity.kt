package com.example.bdodonggumbyul.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils.lastIndexOf
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.example.bdodonggumbyul.R
import com.example.bdodonggumbyul.adapter.SetTagAdapter
import com.example.bdodonggumbyul.databinding.ActivitySetTagBinding
import com.example.bdodonggumbyul.model.UserData
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlin.properties.Delegates

class SetTagActivity : AppCompatActivity() {

    val binding: ActivitySetTagBinding by lazy { ActivitySetTagBinding.inflate(layoutInflater) }
    val rvm = FlexboxLayoutManager(this@SetTagActivity)

    val gson = GsonBuilder().create()
    val pref by lazy { this@SetTagActivity.getSharedPreferences("user_data", Context.MODE_PRIVATE) }
    val editor: SharedPreferences.Editor by lazy { pref.edit() }

    var tagMap = mutableMapOf<String, String>()
    var tags = mutableListOf<String>()
    var keys = mutableListOf<String>()
    lateinit var tagAdapter: SetTagAdapter
    var index by Delegates.notNull<Int>()
    val id: Int by lazy {
        gson.fromJson(
            pref.getString("user_data", ""),
            UserData::class.java
        ).seq.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setRecycler()

        //done cancel 버튼 누르면 선택상태 저장하고 액티비티 종료
        binding.btnDone.setOnClickListener { doneEvent() }
//        binding.btnDelete.setOnClickListener { deleteTag() }
        //rv내 태그 선택시 다중선택가능&색상 변화

        binding.tagEt.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.tagEt.windowToken, 0)
                true
            }
            false
        }
    }

    var isSelected = arrayListOf<String>()
    var isSelectedKey = arrayListOf<String>()
    fun setRecycler() {
        //et 작성후 submit하면 rv에 태그 추가
        //엔터후에 즉시 item list의 리프레시가 일어나지 않음
        binding.tagEt.setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> { //엔터가 두번 눌리는듯
                    if (event.action == KeyEvent.ACTION_DOWN) { //분기처리해서 중복입력 방지 //내가 해냄
                        val item = binding.tagEt.text.toString()
                        tagMap[index.toString()] = item
                        editor.putString("tag_list", gson.toJson(tagMap)).commit()
                        tagAdapter.notifyDataSetChanged()
                        binding.tagEt.text.clear()
                        index++
                        Log.d("@@@@pref 데이터 추가 확인", "${pref.getString("tag_list", "")}")
                    }
                    true
                }
                else -> {
                    Log.d("####error", keyCode.toString())
                    false
                }
            }
        }

        tagMap = gson.fromJson(pref.getString("tag_list", "")!!, tagMap::class.java)
        tagMap["0"] = "전체"
        keys = tagMap.keys.toMutableList()
        tags = tagMap.values.toMutableList()

        tagAdapter = SetTagAdapter(tags)
        index = tagMap.keys.last().toInt() + 1

        binding.rvTag.adapter = tagAdapter
        rvm.justifyContent = JustifyContent.CENTER
        binding.rvTag.layoutManager = rvm

        tagAdapter.setTagClickListener(object : SetTagAdapter.OnTagClickListener {
            override fun onClick(view: View, position: Int) {
                when (view.isSelected) {
                    false -> {
                        if (position == 0) {
                            isSelected.clear()
                            isSelectedKey.clear()

                            isSelected.add("")
                            isSelectedKey.add("")

                            //전체 태그를 재외한 모든 태그를 미선택 상태로 전환
                            for (i in 1 until tags.size) {
                                binding.rvTag[i].isSelected = false
                                Log.d("@@@rv 텍스트", "$i + ${binding.rvTag[i].isSelected}")
                            }
                            view.isSelected = true
                            Log.d("@@전체태그 선택", "$isSelected + $isSelectedKey")
                        } else {
                            isSelected.remove("")
                            isSelectedKey.remove("")

                            isSelected.add(tags[position])
                            isSelectedKey.add(keys[position])

                            binding.rvTag[0].isSelected = false
                            view.isSelected = true
                            Log.d("@@개별태그 선택", "$isSelected + $isSelectedKey")
                        }
                    }
                    true -> {
                        view.isSelected = false
                        isSelected.remove(tags[position])
                        isSelectedKey.remove(keys[position])
                    }
                }
                when {
                    !isSelected.contains("") -> {
                        binding.btnDelete.setImageResource(R.drawable.baseline_delete_forever_24)
                    }
                    else -> {
                        binding.btnDelete.setImageResource(R.drawable.baseline_exit_to_app_24)
                    }
                }
                if (binding.btnDelete.drawable.equals(R.drawable.baseline_exit_to_app_24)){
                    binding.btnDelete.setOnClickListener { finish() }
                }else{
                    binding.btnDelete.setOnClickListener { deleteTag() }
                }
            }
        })
    }

    fun deleteTag() {
        val dialog = AlertDialog.Builder(this@SetTagActivity)
            .setMessage("$isSelected 태그를 삭제합니다.\n해당 태그를 포함한 메모들에서도 태그가 삭제됩니다.\n 계속하시겠습니까?")
            .setPositiveButton("네", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    for (i in isSelectedKey) {
                        if (i != "0") {
                            tagMap.remove(i)
                            tagAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(
                                this@SetTagActivity,
                                "전체 태그는 삭제할 수 없습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Log.d("@@@map 데이터 삭제 확인", "${tagMap.values}")
                    editor.remove("tag_list").commit()
                    editor.putString("tag_list", gson.toJson(tagMap)).commit()
                    Log.d("@@@@pref 데이터 삭제 확인", "${pref.getString("tag_list", "")}")
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
        val intent = Intent(this@SetTagActivity, MainActivity::class.java)
        intent.putExtra("tags", gson.toJson(isSelected))
        Log.d("@@@보내는 태그 확인", "$isSelected")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}