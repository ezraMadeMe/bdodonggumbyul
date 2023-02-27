package com.example.bdodonggumbyul.retrofit

import com.example.bdodonggumbyul.MemoItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface RetrofitService {

    companion object {
        private const val BASEURL = "http://ezra2022.dothome.co.kr"

        val gson: Gson = GsonBuilder()
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

    //로그인 메서드
    @FormUrlEncoded
    @POST("memo/addUser.php")
    fun addUser(
        @Field("userId") userId: String,
        @Field("nickname") nickname: String,
        @Field("password") password: String
    ): Call<String>

    @DELETE("memo/deleteMemo.php")
    fun deleteMemo(
        @Query("memo_id") memoId: String
    ): Call<String>

    //메모 업로드
    @Multipart
    @POST("memo/insertMemo.php")
    fun insertMemo(
        @PartMap dataPart: Map<String, String>,
        @Part filePart: MultipartBody.Part?
    ): Call<String>

    //유저의 모든 메모 가져오기
    @GET("memo/loadAll.php")
    fun loadAll(
        @Query("id") id: Int
    ): Call<MutableList<MemoItem>>

    //유저의 특정일자 메모 가져오기
    @GET("memo/queryDate.php")
    fun queryDate(
        @Query("id") id: Int,
        @Query("date") date: String
    ): Call<MutableList<MemoItem>>


    //특정 키워드가 포함된 메모를 쿼리
    @GET("memo/queryMemo.php")
    fun queryKeyword(
        @Query("id") id: Int,
        @Query("content") content: String
    ): Call<MutableList<MemoItem>>

    //특정 태그가 포함된 메모를 쿼리
    @GET("memo/queryTag.php")
    fun queryTag(
        @Query("id") id: Int,
        @Query("tag") tag: String
    ): Call<MutableList<MemoItem>>
}