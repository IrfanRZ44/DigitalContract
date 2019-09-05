package com.pertamina.digitalcontract

import com.google.gson.annotations.SerializedName

open class ResultContract {
    @field:SerializedName("response")
    open var response : MutableList<Contract> = ArrayList()
}