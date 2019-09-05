package com.pertamina.digitalcontract

import com.google.gson.annotations.SerializedName

open class Document {
    @field:SerializedName("CONTRACT_ID")
    open var CONTRACT_ID : String? = ""
    @field:SerializedName("CONTRACT_NO")
    open var CONTRACT_NO : String? = ""
    @field:SerializedName("CONTRACT_TITLE")
    open var CONTRACT_TITLE : String? = ""
    @field:SerializedName("CONTRACT_CONTENT")
    open var CONTRACT_CONTENT : String? = ""
    @field:SerializedName("TEMPLATE_ID")
    open var TEMPLATE_ID : String? = ""
    @field:SerializedName("COMPILED")
    open var COMPILED : String? = ""
    @field:SerializedName("PDF_PATH")
    open var PDF_PATH : String? = ""
    @field:SerializedName("PUBLISHED")
    open var PUBLISHED : String? = ""
    @field:SerializedName("PUBLISHED_DATETIME")
    open var PUBLISHED_DATETIME : String? = ""
    @field:SerializedName("REVIEWER_ID")
    open var REVIEWER_ID : String? = ""
    @field:SerializedName("REVIEWER_STATUS")
    open var REVIEWER_STATUS : String? = ""
    @field:SerializedName("REVIEWER_NOTE")
    open var REVIEWER_NOTE : String? = ""
    @field:SerializedName("REVIEWER_DATETIME")
    open var REVIEWER_DATETIME : String? = ""
    @field:SerializedName("LEGAL_ID")
    open var LEGAL_ID : String? = ""
    @field:SerializedName("LEGAL_STATUS")
    open var LEGAL_STATUS : String? = ""
    @field:SerializedName("LEGAL_NOTE")
    open var LEGAL_NOTE : String? = ""
    @field:SerializedName("LEGAL_DATETIME")
    open var LEGAL_DATETIME : String? = ""
    @field:SerializedName("FINANCE_ID")
    open var FINANCE_ID : String? = ""
    @field:SerializedName("FINANCE_STATUS")
    open var FINANCE_STATUS : String? = ""
    @field:SerializedName("FINANCE_NOTE")
    open var FINANCE_NOTE : String? = ""
    @field:SerializedName("FINANCE_DATETIME")
    open var FINANCE_DATETIME : String? = ""
    @field:SerializedName("VENDOR_ID")
    open var VENDOR_ID : String? = ""
    @field:SerializedName("VENDOR_SIGNATURE")
    open var VENDOR_SIGNATURE : String? = ""
    @field:SerializedName("VENDOR_CERTIFICATE")
    open var VENDOR_CERTIFICATE : String? = ""
    @field:SerializedName("VENDOR_DATETIME")
    open var VENDOR_DATETIME : String? = ""
    @field:SerializedName("OFFICER_ID")
    open var OFFICER_ID : String? = ""
    @field:SerializedName("OFFICER_SIGNATURE")
    open var OFFICER_SIGNATURE : String? = ""
    @field:SerializedName("OFFICER_CERTIFICATE")
    open var OFFICER_CERTIFICATE : String? = ""
    @field:SerializedName("OFFICER_DATETIME")
    open var OFFICER_DATETIME : String? = ""
    @field:SerializedName("ACTIVE")
    open var ACTIVE : String? = ""
    @field:SerializedName("DELETED")
    open var DELETED : String? = ""
    @field:SerializedName("CREATED_ON")
    open var CREATED_ON : String? = ""
    @field:SerializedName("UPDATED_ON")
    open var UPDATED_ON : String? = ""
    @field:SerializedName("UPDATED_BY")
    open var UPDATED_BY : String? = ""
    @field:SerializedName("LEGAL_SLA")
    open var LEGAL_SLA : String? = ""
    @field:SerializedName("FINANCE_SLA")
    open var FINANCE_SLA : String? = ""
    @field:SerializedName("VENDOR_SLA")
    open var VENDOR_SLA : String? = ""
    @field:SerializedName("OFFICER_SLA")
    open var OFFICER_SLA : String? = ""
}