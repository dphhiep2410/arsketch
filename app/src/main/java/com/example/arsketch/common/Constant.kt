package com.example.arsketch.common

import com.example.arsketch.BuildConfig

object Constant {
    const val KEY_IMAGE_DRAW ="KEY_IMAGE_DRAW"
    const val KEY_RATE_TIME = "KEY_RATE_TIME"
    const val KEY_PASS_ONBOARD="KEY_PASS_ONBOARD"
    const val KEY_FROM_MAIN="KEY_FROM_MAIN"
    const val KEY_DOWNLOADED_MAP ="KEY_DOWNLOADED_MAP"
    const val KEY_IS_CAMERA = "KEY_IS_CAMERA"
    const val KEY_TORCH = "KEY_TORCH"
    const val KEY_SKETCH_MODEL = "KEY_SKETCH_MODEL"
    const val KEY_SKETCH_CATEGORY = "KEY_SKETCH_CATEGORY"
    const val RECORD_TYPE = "RECORD_TYPE"
    const val KEY_NUM_NOTI = "KEY_NUM_NOTI"
    const val KEY_LIST_UNLOCK_CALL = "KEY_LIST_UNLOCK_CALL"
    const val KEY_INDEX_NOTIFY = "KEY_INDEX_NOTIFY"
    const val KEY_RATE_SESSION = "KEY_RATE_SESSION"
    const val KEY_RATE_SESSION_D = "KEY_RATE_SESSION_D"
    const val KEY_RATE = "KEY_RATE"
    const val KEY_NOTIFY = "KEY_NOTIFY"
    const val KEY_LANGUAGE = "KEY_LANGUAGE"
    const val KEY_PASS_LANGUAGE = "KEY_PASS_LANGUAGE"
    const val KEY_SET_LANG = "KEY_SET_LANG"
    const val ALARM_ID = "ALARM_ID"
    const val NOTI_EVERYDAY_EVENING = "NOTI_EVERYDAY_EVENING"
    const val CHANNEL_ID = "CHANNEL_ID_DOG"
    const val CHANNEL_NAME = "CHANNEL_NAME_DOG"
    const val KEY_NOTIFY_POS = "KEY_NOTIFY_DOG"


    val keyData = "https://raw.githubusercontent.com/dphoanghiep1998/ar_sources/main/data.json"


    val URL_APP = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    val PRIVACY = "https://sites.google.com/view/futurepubpolicy"


    val listColor = mutableListOf<String>(
        "#00000000", "#4CB632", "#9E9E9E",
        "#B67132",
        "#D9D9D9",
        "#17110B",
        "#2FEFAA",
        "#FF1585",
        "#C767FF",
        "#FFA510",
        "#3786FB",
        "#018786",
        "#BB86FC",
        "#6200EE",
        "#3700B3"
    )


}