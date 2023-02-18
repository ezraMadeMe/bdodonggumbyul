package com.example.bdodonggumbyul

import com.google.gson.annotations.SerializedName

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

