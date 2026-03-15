package com.example.arsketch.common

import android.os.Build

fun buildMinVersionR(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun buildMinVersionQ(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun buildMinVersionSV2(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2

fun buildMinVersionP(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

fun buildMinVersionS(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun buildVersionP(): Boolean = Build.VERSION.SDK_INT == Build.VERSION_CODES.P

fun buildMinVersionN(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun buildMinVersionT(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun buildMaxVersionN(): Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N

fun buildMinVersionM(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun buildMinVersionO(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

//fun buildMinVersionT(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun buildMinVersionJellyBeanMr1(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

fun buildMinVersionMR1(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

