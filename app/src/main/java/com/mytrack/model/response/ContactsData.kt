package com.mytrack.model.response

import com.google.gson.annotations.SerializedName

data class ContactsData (

  @SerializedName("name") var name: String? = null,
  @SerializedName("mobileno") var mobileno: String? = null,
  @SerializedName("Id") var Id : String? = null,
  @SerializedName("createrName") var createrName: String? = null,
  @SerializedName("createrNo") var createrNo: String? = null,

)