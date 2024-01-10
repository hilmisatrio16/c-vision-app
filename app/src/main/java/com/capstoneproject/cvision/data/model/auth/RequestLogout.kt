package com.capstoneproject.cvision.data.model.auth


import com.google.gson.annotations.SerializedName

data class RequestLogout(
    @SerializedName("username")
    val username: String
)