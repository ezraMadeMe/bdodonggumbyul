package com.example.bdodonggumbyul

import com.google.gson.annotations.SerializedName

//캘린더의 데이터클래스

//파일검색하는 단축키 찾아보기(컨슆o)

//배열을 넘길 수 있지만 gson 써야함
//db에 object list를 넘기는 법 검색
//RoomDB에 db list를 넣고 빼는 법 검색(tag)

data class MemoItem(
    @SerializedName("id")
    var id: String,
    @SerializedName("date")
    var date: String,
    @SerializedName("timestamp")
    var timestamp: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("image")
    var image: String?
)

data class TagItem(
    @SerializedName("id")
    var id: String,
    @SerializedName("tags")
    var tags: MutableList<String>?
)

