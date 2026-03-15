package com.dream.libads.utils

import android.content.Context
import android.content.SharedPreferences

class AdsConfigUtils(context: Context) {
    companion object {

        const val ADMOB = 1
        const val OFF = 0

        const val open_resume = "open_resume"
        const val native_full_status = "native_full_status"
        const val native_splash = "native_splash"
        const val native_full_setting = "native_full_setting"
        const val native_medium_category = "native_medium_category"
        const val native_ob3 = "native_ob3"
        const val native_category_2 = "native_category_2"
        const val native_collapse = "native_collapse"
        const val native_draw = "native_draw"
        const val native_full_ob1 = "native_full_ob1"
        const val native_full_ob2 = "native_full_ob2"
        const val native_permission = "native_permission"
        const val native_ob1 = "native_ob1"
        const val native_ob2 = "native_ob2"
        const val native_lang = "native_lang"
        const val time_out_show_ads = "time_out_show_ads"
        const val native_full_splash = "native_full_splash"
        const val native_full_ob3 = "native_full_ob3"
        const val native_setting = "native_setting"
        const val inter_splash = "inter_splash"
        const val inter_home = "inter_home"
        const val inter_back = "inter_back"
        const val inter_draw = "inter_draw"
        const val inter_ob3 = "inter_ob3"


    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AdsConfigUtils", Context.MODE_PRIVATE)

    val listKey = mutableListOf(
        native_full_status,
        inter_home,
        inter_back,
        inter_draw,
        inter_ob3,
        inter_splash,
        open_resume,
        native_splash,
        native_setting,
        native_full_setting,
        native_medium_category,
        native_ob3,
        native_category_2,
        native_collapse,
        native_draw,
        native_full_ob1,
        native_full_ob2,
        native_permission,
        native_ob1,
        native_ob2,
        native_lang,
        native_full_splash,
        native_full_ob3,
    )

    val listDefaultValue = mutableListOf(
        ADMOB,
    )


    fun getTimeoutShowAds(): Int = sharedPreferences.getInt(time_out_show_ads, 20000)

    fun getStatusConfig(key: String): Int {
        return sharedPreferences.getInt(key,
            ADMOB
        )
    }
    fun getPremium(): Boolean {
        return sharedPreferences.getBoolean("isPremium", false)
    }

    fun putValue(mKey: String, mValue: Int? = null) {

        val myMap = mutableMapOf<String, Int>()
        listKey.forEachIndexed { index, key ->
            myMap[key] = ADMOB
        }

        val editor = sharedPreferences.edit()
        if (mValue == null) {
            editor.putInt(mKey, myMap[mKey]!!)
        } else {
            editor.putInt(mKey, mValue)
        }
        editor.apply()

    }


    fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }


}