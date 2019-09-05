package com.pertamina.digitalcontract

object Config {
    val SHARED_PREF = ""
//    val BASE_URL = "http://pertamina.kabirland.technology/"
    val BASE_URL = "http://mor7.com/digital_contract/"
//    val BASE_URL = "https://digitalcontract.mor7.com/"
//    val BASE_URL = "http://192.168.0.103/pertamina/"
    val BASE_URL_API = BASE_URL+"api/v1/json_sender/"
    val BASE_URL_BSRE = BASE_URL+"api/v1/json_sender2/"

//    val BASE_URL_BSRE = "https://esign-dev.bssn.go.id/"
    val BSRE_CLIENT_ID = "16314426"
    val BSRE_CLIENT_SECRET = "39j1-sifb-del9-3cg6"
}

enum class Stage { FINGERPRINT, NEW_FINGERPRINT_ENROLLED, PASSWORD }
@JvmField val DEFAULT_KEY_NAME = "default_key"