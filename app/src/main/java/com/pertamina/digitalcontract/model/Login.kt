package com.pertamina.digitalcontract

import com.google.gson.annotations.SerializedName

open class Login {
    @field:SerializedName("response")
    open var response : Int? = 0
    @field:SerializedName("id")
    open var id : String? = ""
    @field:SerializedName("name")
    open var name : String? = ""
    @field:SerializedName("role")
    open var role : String? = ""
    @field:SerializedName("imei")
    open var imei : String? = ""


}