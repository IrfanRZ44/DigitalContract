package com.pertamina.digitalcontract

import com.google.gson.annotations.SerializedName

open class Contract {
    @field:SerializedName("CONTRACT_ID")
    open var CONTRACT_ID : String? = ""
    @field:SerializedName("CONTRACT_TITLE")
    open var CONTRACT_TITLE : String? = ""
    @field:SerializedName("CREATED_ON")
    open var CREATED_ON : String? = ""
    @field:SerializedName("LEGAL_STATUS")
    open var LEGAL_STATUS : String? = ""
    @field:SerializedName("REVIEWER_STATUS")
    open var REVIEWER_STATUS : String? = ""
    @field:SerializedName("FINANCE_STATUS")
    open var FINANCE_STATUS : String? = ""
    @field:SerializedName("VENDOR_SIGNATURE")
    open var VENDOR_SIGNATURE : String? = ""
    @field:SerializedName("VENDOR_CERTIFICATE")
    open var VENDOR_CERTIFICATE : String? = ""
    @field:SerializedName("OFFICER_SIGNATURE")
    open var OFFICER_SIGNATURE : String? = ""
    @field:SerializedName("OFFICER_CERTIFICATE")
    open var OFFICER_CERTIFICATE : String? = ""
    @field:SerializedName("PDF_PATH")
    open var PDF_PATH : String? = ""
    @field:SerializedName("LEGAL_ID")
    open var LEGAL_ID : String? = ""
    @field:SerializedName("FINANCE_ID")
    open var FINANCE_ID : String? = ""
    @field:SerializedName("HSSE_ID")
    open var HSSE_ID : String? = ""
    @field:SerializedName("HSSE_STATUS")
    open var HSSE_STATUS : String? = ""
    @field:SerializedName("FUNGSI_ID")
    open var FUNGSI_ID : String? = ""
    @field:SerializedName("REVIEWER_ID")
    open var REVIEWER_ID : String? = ""
    @field:SerializedName("PUBLISHED")
    open var PUBLISHED : String? = ""




}