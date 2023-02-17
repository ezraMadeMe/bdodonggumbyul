package com.example.bdodonggumbyul

import com.google.gson.annotations.SerializedName

data class Memo(
    @SerializedName("date")
    var date: String,
    @SerializedName("timestamp")
    var timestamp: String,
    @SerializedName("memo")
    var memo: String
)


