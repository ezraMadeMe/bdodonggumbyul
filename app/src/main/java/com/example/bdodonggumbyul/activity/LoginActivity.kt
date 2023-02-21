package com.example.bdodonggumbyul.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bdodonggumbyul.databinding.ActivityLoginBinding
import com.example.bdodonggumbyul.model.User
import com.example.bdodonggumbyul.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity: AppCompatActivity() {

    val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    lateinit var pref: SharedPreferences
    lateinit var editor :SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pref = this@LoginActivity.getSharedPreferences("userData",Context.MODE_PRIVATE)
        editor = pref.edit()

        binding.btnLogin.setOnClickListener { loginLogic() }
        when(pref.getString("userId","")){
            null,"" -> {  }
            else -> {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("nickname",binding.etNick.text.toString())
                startActivity(intent)
            }
        }
    }

    fun loginLogic(){
        val userId = binding.etId.text.toString()
        val nickname = binding.etNick.text.toString()
        val password = binding.etPw.text.toString()

        val retrofitService = RetrofitService.newInstance()
        val call = retrofitService.addUser(userId, nickname, password)

        Log.d("@@@닉네임 값 확인",nickname)

        call.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val result = response.body()
                if (result != null){
                        Log.d("@@@@레트로핏 결과 확인","$result")
                    Toast.makeText(this@LoginActivity, "${result}", Toast.LENGTH_SHORT).show()
                    if(result.equals("회원가입 성공!")){
                        editor.putString("userId",userId)
                        editor.putString("nickname",nickname)
                        editor.commit()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("nickname",binding.etNick.text.toString())
                        startActivity(intent)
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("@@@@레트로핏 실패 확인","${t.message}")
            }
        })
    }
}