package com.capstoneproject.cvision.data.model.predict


import com.google.gson.annotations.SerializedName

data class ResponsePredict(
    @SerializedName("Error")
    val error: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("Prediction")
    val prediction: String
)