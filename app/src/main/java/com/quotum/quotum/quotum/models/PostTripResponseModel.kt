package com.quotum.quotum.quotum.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostTripResponseModel {
    @Expose
    @SerializedName("currency")
    var currency: String? = null

    @Expose
    @SerializedName("pictures")
    var pictures: List<String>? = null

    @Expose
    @SerializedName("details")
    var details: List<String>? = null

    @Expose
    @SerializedName("persons")
    var persons: Persons? = null

    @Expose
    @SerializedName("modeOfTransport")
    var modeOfTransport: String? = null

    @Expose
    @SerializedName("budget")
    var budget = 0

    @Expose
    @SerializedName("created")
    var created: String? = null

    @Expose
    @SerializedName("endDate")
    var endDate: String? = null

    @Expose
    @SerializedName("startDate")
    var startDate: String? = null

    @Expose
    @SerializedName("destination")
    var destination: String? = null

    @Expose
    @SerializedName("source")
    var source: String? = null

    @Expose
    @SerializedName("location")
    var location: Location? = null

    class Persons {
        @Expose
        @SerializedName("children")
        var children = 0

        @Expose
        @SerializedName("adult")
        var adult = 0
    }

    class Location {
        @Expose
        @SerializedName("lng")
        var lng = 0

        @Expose
        @SerializedName("lat")
        var lat = 0
    }
}