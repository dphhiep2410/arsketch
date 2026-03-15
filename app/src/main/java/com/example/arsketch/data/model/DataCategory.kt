package com.example.arsketch.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataCategory(val id: Int, val name: String,val image:Int):Parcelable {
}