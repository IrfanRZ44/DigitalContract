package com.pertamina.digitalcontract.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.pertamina.digitalcontract.activity.ActLogin

class SessionManager(internal var _context: Context) {
    // Shared Preferences
    internal var pref: SharedPreferences

    // Editor for Shared preferences
    internal var editor: SharedPreferences.Editor

    // Shared pref mode
    internal var PRIVATE_MODE = 0

    val isLoggedIn: Boolean
        get() = pref.getBoolean(IS_LOGIN, false)

    val username : String?
        get() = pref.getString(K_USERNAME, null)

    val password : String?
        get() = pref.getString(K_PASSWORD, null)

    val id : String?
        get() = pref.getString(K_ID, null)

    val name : String?
        get() = pref.getString(K_NAME, null)

    val imei: String?
        get() = pref.getString(K_IMEI, null)

    val role: String?
        get() = pref.getString(K_ROLE, null)

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun createLoginSession(username : String, password : String, id : String, name : String, imei : String, role : String) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(K_USERNAME, username)
        editor.putString(K_PASSWORD, password)
        editor.putString(K_ID, id)
        editor.putString(K_NAME, name)
        editor.putString(K_IMEI, imei)
        editor.putString(K_ROLE, role)



        // commit changes
        editor.commit()
    }

    fun checkLogin() {
        // Check login status
        if (!this.isLoggedIn) {
            // user is not logged in redirect him to ActLogin Activity
            val i = Intent(_context, ActLogin::class.java)
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Add new Flag to start new Activity
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Staring ActLogin Activity
            _context.startActivity(i)
        }
    }

    fun logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear()
        editor.commit()

        // After logout redirect user to Loing Activity
        val i = Intent(_context, ActLogin::class.java)
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        // Add new Flag to start new Activity
//        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // Staring ActLogin Activity
        _context.startActivity(i)
    }

    companion object {

        // Sharedpref file name
        private val PREF_NAME = "PertaminaDC_Pref"

        // All Shared Preferences Keys
        private val IS_LOGIN = "IsLoggedIn"
        private val K_USERNAME = "inputUser"
        private val K_PASSWORD = "inputPassword"
        private val K_ID = "id"
        private val K_NAME = "name"
        private val K_IMEI = "imei"
        private val K_ROLE = "role"

        private var instance: SessionManager? = null

        fun with(context: Context): SessionManager {

            if (instance == null) {
                instance = SessionManager(context)
            }
            return instance as SessionManager
        }
    }

}