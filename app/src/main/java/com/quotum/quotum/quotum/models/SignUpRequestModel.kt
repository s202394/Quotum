package com.quotum.quotum.quotum.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignUpRequestModel {
    @Expose
    @SerializedName("deviceType")
    var deviceType: String? = null

    @Expose
    @SerializedName("deviceToken")
    var deviceToken: String? = null

    @Expose
    @SerializedName("userData")
    var userData: UserData? = null

    class UserData {
        @Expose
        @SerializedName("password")
        var password: String? = null

        @Expose
        @SerializedName("lastName")
        var lastName: String? = null

        @Expose
        @SerializedName("firstName")
        var firstName: String? = null

        @Expose
        @SerializedName("mobileNumber")
        var mobileNumber: String? = null

        @Expose
        @SerializedName("email")
        var email: String? = null
    }
}