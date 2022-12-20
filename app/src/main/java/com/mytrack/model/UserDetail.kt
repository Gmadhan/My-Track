package com.mytrack.model

import com.google.gson.annotations.SerializedName

data class UserDetail (

  @SerializedName("gps_Lng") var gpsLng: Double? = null,
  @SerializedName("name") var name: String? = null,
  @SerializedName("imageEncoded") var imageEncoded : String? = null,
  @SerializedName("model") var model: String? = null,
  @SerializedName("gps_Lat") var gpsLat: Double? = null,
  @SerializedName("userId") var userId: String? = null,
  @SerializedName("email") var email: String? = null,
  @SerializedName("phoneno") var phoneno: String?    = null,
  @SerializedName("encryptpwd") var encryptpwd: String? = null,
  @SerializedName("token") var token: String? = null

)