package com.dream.libads.utils

import android.util.Log

object AppLogger {
    const val LOG_TAG = "AppLogger "
    var startTime = -1L
    private val hashMap = HashMap<String, Long>()
    fun d(message: String? = "empty") {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.d(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber,
            LOG_TAG + (message.takeIf { it?.isNotEmpty() == true } ?: "empty")
        )
    }

    fun d(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.d(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
        )
    }

    fun e(message: Long? = 0) {
        Log.e(
            LOG_TAG, message.toString()
        )
    }

    fun checkPoint(key: String? = null) {
        if (key != null) {
            hashMap[key] = System.currentTimeMillis()
        } else {
            startTime = System.currentTimeMillis()
        }
    }

    fun eCheckPoint(message: String? = "empty", key: String? = null) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.e(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber,
            LOG_TAG + ("timeCheck = ${System.currentTimeMillis() - if (hashMap[key] != null) hashMap[key]!! else startTime}") + (message.takeIf { it?.isNotEmpty() == true }
                ?: "empty")
        )
        startTime = -1
        hashMap.remove(key)
    }

    fun e(message: String? = "empty") {
        Log.e(LOG_TAG, (message.takeIf { it?.isNotEmpty() == true } ?: "empty")
        )
    }

    fun e(error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.e(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + "error: ", error
        )
    }

    fun e(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.e(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
        )
    }

    fun i(message: String) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.i(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message
        )
    }

    fun i(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.i(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
        )
    }

    fun w(message: String) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.w(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message
        )
    }

    fun w(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        Log.w(
            stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                    " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
        )
    }
}