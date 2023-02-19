package com.example.bdodonggumbyul.retrofit

import com.example.bdodonggumbyul.MemoItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface RetrofitService {

    companion object {
        private const val BASEURL = "http://ezra2022.dothome.co.kr"

        val gson : Gson = GsonBuilder()
            .setLenient()
            .create()

        fun newInstance(): RetrofitService {
            return Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService::class.java)
        }
    }

    //메모 업로드
    @Multipart
    @POST("memo/insertMemo.php")
    fun insertMemo(@PartMap dataPart: Map<String, String>,
                    @Part filePart: MultipartBody.Part?): Call<String>

    //전체 메모 가져오기
    @GET("memo/loadMemo.php")
    fun loadMemo(): Call<MutableList<MemoItem>>

    //특정 키워드가 포함된 메모를 쿼리
    @GET("memo/queryMemo.php")
    fun queryMemo(@Query("content") content: String): Call<MutableList<MemoItem>>

    //특정 날짜의 메모를 쿼리
    //초기 로딩시 오늘 날짜
    @GET("memo/queryDate.php")
    fun queryDate(@Query("date") date: String): Call<MutableList<MemoItem>>

    //특정 태그가 포함된 메모를 쿼리

    //작성된 새로운 메모를 서버에 추가
}