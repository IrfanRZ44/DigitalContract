package com.pertamina.digitalcontract

object Config {
    val BASE_URL = "https://digitalcontractv3.mor7.com/"

    val BASE_URL_BSRE = BASE_URL+"api/v1/Json_sender2/"

    val BASE_URL_API = BASE_URL+"api/v1/Json_sender/"
}

enum class Stage { FINGERPRINT, NEW_FINGERPRINT_ENROLLED, PASSWORD }
@JvmField val DEFAULT_KEY_NAME = "default_key"