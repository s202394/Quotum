package com.quotum.quotum.quotum.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginRequestModel {
    @Expose
    @SerializedName("email")
    var email: String? = null

    @Expose
    @SerializedName("password")
    var password: String? = null

    @Expose
    @SerializedName("deviceToken")
    var deviceToken: String? = null

    @Expose
    @SerializedName("deviceType")
    var deviceType: String? = null
}