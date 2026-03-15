package com.dream.libads.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.libads.BuildConfig

/*
 * @author: GiangFpt191195@gmail.com
 * create: 19/12/2023
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {
    private var showingForm = false
    private var isTimeExpired = false

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)
    private val cmpUtils: CMPUtils = CMPUtils(context)

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(
        activity: Activity,
        onCanShowAds: (() -> Unit),
        onDisableAds: (() -> Unit),
    ) {
        val params = if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("374B5A745C772D338AFB6387460CBE6E") // Get ID from Logcat
                .build()
            ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(debugSettings).build()
        } else {
            ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        }
        isTimeExpired = false
        var handler: Handler? = Handler()
        val runnable = {
            if (handler != null) {
                Log.d("TAG", "isTimeExpired: ")
                onDisableAds.invoke()
                isTimeExpired = true
                handler = null
            }

        }
        handler?.postDelayed(runnable, Constants.TIME_OUT_DELAY)

        consentInformation.requestConsentInfoUpdate(activity, params, {
            Log.d("TAG", "gatherConsent: ")
            if (isPrivacyOptionsRequired) {
                if (!cmpUtils.canShowAds()) {
                    if (showingForm) return@requestConsentInfoUpdate
                    showingForm = true
                    if (!isTimeExpired) {
                        UserMessagingPlatform.loadConsentForm(activity, {
                            handler?.removeCallbacks(runnable)
                            handler = null
                            if (!isTimeExpired) {
                                it.show(activity) {
                                    showingForm = false
                                    Log.e(
                                        "NekoAdsLoader",
                                        "showConsentFormInPlash FormError: ${it?.errorCode}: ${it?.message}"
                                    )
                                    if (!isTimeExpired) {
                                        if (cmpUtils.canShowAds()) {
                                            onCanShowAds.invoke()
                                        } else {
                                            onDisableAds.invoke()
                                        }
                                    }

                                }
                            }

                        }, {
                            handler?.removeCallbacks(runnable)
                            handler = null
                            showingForm = false
                            Log.e(
                                "NekoAdsLoader",
                                "showConsentFormInPlash FormError: ${it?.errorCode}: ${it?.message}"
                            )
                            if (!isTimeExpired) {
                                if (cmpUtils.canShowAds()) {
                                    onCanShowAds.invoke()
                                } else {
                                    onDisableAds.invoke()
                                }
                            }
                        })
                    }

                } else {
                    handler?.removeCallbacks(runnable)
                    handler = null
                    if (!isTimeExpired) {
                        onCanShowAds.invoke()
                    }
                }
            } else {
                handler?.removeCallbacks(runnable)
                handler = null
                Log.e("CMP", "Không nằm trong vùng Yêu cầu")
                if (!isTimeExpired) {
                    onCanShowAds.invoke()
                }
            }
        }, {
            handler?.removeCallbacks(runnable)
            handler = null
            if (isPrivacyOptionsRequired) {
                if (!isTimeExpired) {
                    if (cmpUtils.canShowAds()) {
                        onCanShowAds.invoke()
                    } else {
                        onDisableAds.invoke()
                    }
                }
            } else {
                if (!isTimeExpired) {
                    onCanShowAds.invoke()
                }
            }
        })
    }

    /** Helper method to call the UMP SDK method to show the privacy options form. */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: GoogleMobileAdsConsentManager(context).also {
                instance = it
            }
        }
    }
}