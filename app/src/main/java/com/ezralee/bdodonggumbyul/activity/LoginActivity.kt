package com.ezralee.bdodonggumbyul.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezralee.bdodonggumbyul.databinding.ActivityLoginBinding
import com.ezralee.bdodonggumbyul.model.UserData
import com.ezralee.bdodonggumbyul.retrofit.RetrofitService
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    val pref by lazy { this@LoginActivity.getSharedPreferences("user_data", Context.MODE_PRIVATE) }
    val editor by lazy { pref.edit() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.etPw.setOnKeyListener { v, keyCode, event ->
            if (event.action ==KeyEvent.ACTION_DOWN && keyCode ==KeyEvent.KEYCODE_ENTER){
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etPw.windowToken, 0)
                true
            }
            false
        }

        binding.btnLogin.setOnClickListener { loginLogic() }
        when (pref.getString("user_data", "")) {
            null, "" -> {}
            else -> {
                Log.d("@@json pref 파싱", pref.getString("user_data", "").toString())
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun loginLogic() {
        val userId = binding.etId.text.toString()
        val nickname = binding.etNick.text.toString()
        val password = binding.etPw.text.toString()

        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.addUser(userId, nickname, password)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val result = response.body()
                if (result != null) {
                    Log.d("@@@@레트로핏 결과 확인", "$result")

                    if (!result.equals("이미 존재하는 아이디입니다") && !result.equals("이미 존재하는 닉네임입니다")) {
                        Toast.makeText(this@LoginActivity, "$nickname 님 환영합니다!", Toast.LENGTH_SHORT)
                            .show()

                        val string = mutableMapOf<String, String>()
                        string["0"] = "전체"
                        val gson = GsonBuilder().create()

                        editor.putString("user_data", result)
                            .putString("tag_list", gson.toJson(string))
                            .putString("memo_num","0")
                            .commit()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)

                    } else if (result.equals("이미 존재하는 아이디입니다")) {
                        Toast.makeText(this@LoginActivity, "중복된 아이디입니다", Toast.LENGTH_SHORT).show()

                    } else if (result.equals("이미 존재하는 닉네임입니다")) {
                        Toast.makeText(this@LoginActivity, "중복된 닉네임입니다", Toast.LENGTH_SHORT).show()
                    }
                }
                this@LoginActivity.finish()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("@@@@레트로핏 실패 확인", "${t.message}")
            }
        })
    }
}