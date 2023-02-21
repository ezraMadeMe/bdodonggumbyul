package com.example.bdodonggumbyul.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("password")
    val password: String
)
