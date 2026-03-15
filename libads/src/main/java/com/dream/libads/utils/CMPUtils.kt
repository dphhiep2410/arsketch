package com.dream.libads.utils

import android.content.Context
import android.preference.PreferenceManager
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
class CMPUtils(var applicationContext: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(applicationContext)
    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus !=
                    ConsentInformation.PrivacyOptionsRequirementStatus.NOT_REQUIRED


    fun isGDPR(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gdpr = prefs.getInt("IABTCF_gdprApplies", 0)
        return gdpr == 1
    }

    fun canShowAds(): Boolean {
        if (isPrivacyOptionsRequired) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
            val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
            val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
            val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

            val googleId = 755
            val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
            val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

            // Minimum required for at least non-personalized ads
            return hasConsentFor(listOf(1), purposeConsent, hasGoogleVendorConsent)
                    && hasConsentOrLegitimateInterestFor(
                listOf(2, 7, 9, 10),
                purposeConsent,
                purposeLI,
                hasGoogleVendorConsent,
                hasGoogleVendorLI
            )
        } else {
            return true
        }
    }

    fun canShowPersonalizedAds(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
        val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

        val googleId = 755
        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
        val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

        return hasConsentFor(listOf(1, 3, 4), purposeConsent, hasGoogleVendorConsent)
                && hasConsentOrLegitimateInterestFor(
            listOf(2, 7, 9, 10),
            purposeConsent,
            purposeLI,
            hasGoogleVendorConsent,
            hasGoogleVendorLI
        )
    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private fun hasAttribute(input: String, index: Int): Boolean {
        return input.length >= index && input[index - 1] == '1'
    }

    // Check if consent is given for a list of purposes
    private fun hasConsentFor(
        purposes: List<Int>,
        purposeConsent: String,
        hasVendorConsent: Boolean
    ): Boolean {
        return purposes.all { p -> hasAttribute(purposeConsent, p) } && hasVendorConsent
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private fun hasConsentOrLegitimateInterestFor(
        purposes: List<Int>,
        purposeConsent: String,
        purposeLI: String,
        hasVendorConsent: Boolean,
        hasVendorLI: Boolean
    ): Boolean {
        return purposes.all { p ->
            (hasAttribute(purposeLI, p) && hasVendorLI) ||
                    (hasAttribute(purposeConsent, p) && hasVendorConsent)
        }
    }
}