/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.pertamina.digitalcontract.fingerprint

import android.app.DialogFragment
import android.content.Context
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.Stage

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
class FingerprintAuthenticationDialogFragment : DialogFragment(),
        FingerprintUiHelper.Callback {

    private lateinit var callback: Callback
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var fingerprintUiHelper: FingerprintUiHelper
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var sharedPreferences: SharedPreferences

    private var stage = Stage.FINGERPRINT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        retainInstance = true

        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.setTitle(null)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val v = inflater.inflate(R.layout.dialog_fingerprint, container, false)

        val btCancel = v.findViewById<TextView>(R.id.dialog_universal_warning_cancel)
        btCancel.setOnClickListener { dialog.cancel() }
        return v
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fingerprintUiHelper = FingerprintUiHelper(
                activity.getSystemService(FingerprintManager::class.java),
                view.findViewById(R.id.fingerprint_icon),
                view.findViewById(R.id.fingerprint_status),
                this
        )

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!fingerprintUiHelper.isFingerprintAuthAvailable) {
            callback.noFingerprint()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        fingerprintUiHelper.startListening(cryptoObject)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPause() {
        super.onPause()
        fingerprintUiHelper.stopListening()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager = context.getSystemService(InputMethodManager::class.java)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setCryptoObject(cryptoObject: FingerprintManager.CryptoObject) {
        this.cryptoObject = cryptoObject
    }

    fun setStage(stage: Stage) {
        this.stage = stage
    }

    override fun onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication succeeded.
        callback.onPurchased(withFingerprint = true, crypto = cryptoObject)
        dismiss()
    }

    //kalau auth error
    override fun onError() {
        callback.errorFingerprint()
    }

    interface Callback {
        fun onPurchased(withFingerprint: Boolean, crypto: FingerprintManager.CryptoObject? = null)
        fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean = true)
        fun errorFingerprint()
        fun noFingerprint()
    }
}
