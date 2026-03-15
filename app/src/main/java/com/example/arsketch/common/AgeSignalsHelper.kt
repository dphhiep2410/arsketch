package com.example.arsketch.common

import android.content.Context
import android.util.Log
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus

object AgeSignalsHelper {

    private const val TAG = "AgeSignalsHelper"

    private const val KEY_AGE_LOWER = "age_signals_age_lower"
    private const val KEY_AGE_UPPER = "age_signals_age_upper"
    private const val KEY_USER_STATUS = "age_signals_user_status"
    private const val KEY_IS_MINOR = "age_signals_is_minor"

    /**
     * Check age signals from Google Play.
     * For Brazil (Digital ECA), userStatus will be DECLARED or UNKNOWN.
     * Age ranges: 0-12, 13-15, 16-17, 18+
     */
    fun checkAgeSignals(context: Context, onResult: ((isMinor: Boolean) -> Unit)? = null) {
        try {
            val ageSignalsManager = AgeSignalsManagerFactory.create(context)

            ageSignalsManager
                .checkAgeSignals(AgeSignalsRequest.builder().build())
                .addOnSuccessListener { result ->
                    handleAgeSignalsResult(context, result, onResult)
                }
                .addOnFailureListener { exception ->
                    handleAgeSignalsFailure(exception, onResult)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AgeSignalsManager", e)
            onResult?.invoke(false)
        }
    }

    private fun handleAgeSignalsResult(
        context: Context,
        result: AgeSignalsResult,
        onResult: ((isMinor: Boolean) -> Unit)?
    ) {
        val userStatus = result.userStatus()
        val ageLower = result.ageLower()
        val ageUpper = result.ageUpper()

        Log.d(TAG, "Age signals - status: $userStatus, ageLower: $ageLower, ageUpper: $ageUpper")

        val pref = AppSharePreference.getInstance(context)

        pref.saveInt(KEY_AGE_LOWER, ageLower ?: -1)
        pref.saveInt(KEY_AGE_UPPER, ageUpper ?: -1)
        pref.saveString(KEY_USER_STATUS, userStatus?.toString() ?: "UNKNOWN")

        val isMinor = when (userStatus) {
            AgeSignalsVerificationStatus.SUPERVISED,
            AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED -> true

            AgeSignalsVerificationStatus.DECLARED,
            AgeSignalsVerificationStatus.VERIFIED -> {
                // Check age range - minor if upper bound < 18
                if (ageUpper != null) ageUpper < 18 else false
            }

            else -> false
        }

        pref.saveBoolean(KEY_IS_MINOR, isMinor)
        Log.d(TAG, "User is minor: $isMinor")

        onResult?.invoke(isMinor)
    }

    private fun handleAgeSignalsFailure(
        exception: Exception,
        onResult: ((isMinor: Boolean) -> Unit)?
    ) {
        if (exception is AgeSignalsException) {
            Log.w(TAG, "AgeSignals error code: ${exception.errorCode}, message: ${exception.message}")
        } else {
            Log.w(TAG, "AgeSignals failed", exception)
        }
        // On failure, don't restrict - default to not minor
        onResult?.invoke(false)
    }

    /**
     * Returns true if the user has been identified as a minor.
     */
    fun isMinor(context: Context): Boolean {
        return AppSharePreference.getInstance(context).getBoolean(KEY_IS_MINOR, false)
    }

    /**
     * Returns the cached user status string.
     */
    fun getUserStatus(context: Context): String {
        return AppSharePreference.getInstance(context).getString(KEY_USER_STATUS, "UNKNOWN")
    }

    /**
     * Returns the cached age lower bound, or -1 if not available.
     */
    fun getAgeLower(context: Context): Int {
        return AppSharePreference.getInstance(context).getInt(KEY_AGE_LOWER, -1)
    }

    /**
     * Returns the cached age upper bound, or -1 if not available.
     */
    fun getAgeUpper(context: Context): Int {
        return AppSharePreference.getInstance(context).getInt(KEY_AGE_UPPER, -1)
    }
}
