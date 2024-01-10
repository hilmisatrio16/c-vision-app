package com.capstoneproject.cvision.data.remote

import com.capstoneproject.cvision.data.model.auth.ResponseLogin
import com.capstoneproject.cvision.data.model.auth.ResponseLogout
import com.capstoneproject.cvision.data.model.predict.ResponsePredict
import com.capstoneproject.cvision.data.model.auth.ResponseRegister
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun userLogin(
        @Field("username") username: String,
        @Field("password") password: String
    ): ResponseLogin

    @FormUrlEncoded
    @POST("register")
    suspend fun userRegister(
        @Field("name") name: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): ResponseRegister

    @FormUrlEncoded
    @POST("logout")
    suspend fun userLogout(
        @Field("username") username: String
    ): ResponseLogout


    @Multipart
    @POST("predict")
    suspend fun predictCataract(
        @Part("token") token: RequestBody,
        @Part file: MultipartBody.Part
    ): ResponsePredict
}