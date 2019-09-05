package com.pertamina.digitalcontract

import com.google.gson.annotations.SerializedName

open class ResultDocument {
    @field:SerializedName("response")
    open var response : MutableList<Document> = ArrayList()
}