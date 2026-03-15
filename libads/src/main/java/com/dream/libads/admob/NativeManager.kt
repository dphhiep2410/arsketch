package com.dream.libads.admob

abstract class NativeManager {
    var isAdsLoaded = false

    abstract fun reload(action:(()->Unit)?)
}