package com.mytrack.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class MessageResponse (
    @SerializedName("messageText") var messageText: String? = null,
    @SerializedName("messageUser") var messageUser: String? = null,
    @SerializedName("from") var from: String? = null,
    @SerializedName("imageUrl") var imageUrl: String? = null,
    @SerializedName("messageTime") var messageTime: Long = Date().time
)