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
    //메모 삭제
    @GET("memo/deleteMemo.php")
    fun deleteMemo(
        @Query("id") id: Int,
        @Query("memo_id") memoId: String
    ): Call<String>

    //메모 업로드 또는 업데이트
    @Multipart
    @POST("memo/insertMemo.php")
    fun insertMemo(
        @PartMap dataPart: Map<String, String>,
        @Part filePart: MultipartBody.Part?
    ): Call<String>

    //유저의 모든/특정일자의/특정 태그가 달린/특정 일자+태그가 달린 메모 가져오기
    @GET("memo/queryDateNTag.php")
    fun queryDateNTag(
        @Query("id") id: Int,
        @Query("date") date: String,
        @Query("tag") tag: String
    ): Call<MutableList<MemoItem>>

    //특정 키워드가 포함된 메모를 쿼리
    @GET("memo/queryMemo.php")
    fun queryKeyword(
        @Query("id") id: Int,
        @Query("keyword") keyword: String
    ): Call<MutableList<MemoItem>>
}