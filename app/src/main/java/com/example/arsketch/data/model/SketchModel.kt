package com.example.arsketch.data.model

import android.os.Parcelable
import java.io.File

@kotlinx.parcelize.Parcelize
data class SketchModel(
    val id: Int,
    var localUrl: String,
    val remoteUrl: String,
    val freeIdRawRes: Int,
    var isUnlocked: Boolean,
    var isFree: Boolean
) : Parcelable {

    val isDownLoaded = localUrl.startsWith("content://") || File(localUrl).exists()
}