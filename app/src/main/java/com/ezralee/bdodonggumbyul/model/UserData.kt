package com.ezralee.bdodonggumbyul.model

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("seq")
    var seq: String,
    @SerializedName("userId")
    var userId: String,
    @SerializedName("nickname")
    var nickname: String
)