package com.example.bdodonggumbyul.retrofit

import com.example.bdodonggumbyul.Memo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {

    companion object {
        const val baseUrl = "http://ezra2022.dothome.co.kr/memo/"

        fun newInstance(): RetrofitService {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService::class.java)
        }
    }

    //특정 날짜의 메모를 쿼리
    //초기 로딩시 오늘 날짜
    @GET("./getMemos.php")
    fun getMemos() : Call<Memo>

    //특정 태그가 포함된 메모를 쿼리
    @GET("./getMemoWithTag.php")
    fun getMemoWithTag() : Call<Memo>

    //특정 키워드가 포함된 메모를 쿼리

    //작성된 새로운 메모를 서버에 추가
    @POST("./postNewMemo.php")
    fun postNewMemo(
        @Field("date")
        date: String,
        @Field("timestamp")
        timestamp: String,
        @Field("memo")
        memo: String
    ) : Call<String>
}