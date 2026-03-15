package com.example.arsketch.common

import android.annotation.SuppressLint
import com.example.arsketch.R
import java.util.Locale


object LocaleUtils {
    @SuppressLint("ConstantLocale")
    val defaultLocale: Locale = Locale.getDefault()
    val countryCodes: Set<String> = Locale.getISOCountries().toSet()
    val availableLocales: List<Locale> =
        Locale.getAvailableLocales().filter { countryCodes.contains(it.country) }

    @SuppressLint("ConstantLocale")

    val supportedLocales: MutableList<Locale> = mutableListOf(
        ENGLISH,
        FRENCH,
        INDIA,
        JAPANESE,
        BRAZIL,
        VIETNAM,
        KOREAN,
        TURKEY,
        SPAIN,
        ITALIA,
        GERMAN,
    )
    val supportedLocales1: MutableList<Locale> = mutableListOf(
        FRENCH,
        INDIA,
        JAPANESE,
        BRAZIL,
        VIETNAM,
        KOREAN,
        TURKEY,
        SPAIN,
        ITALIA,
        GERMAN,
        ENGLISH,
    )

    val supportLanguages: MutableList<Triple<Int, Int, Locale>> = mutableListOf(
        Triple(R.string.ENGLISH, R.drawable.ic_language_english, ENGLISH),
        Triple(R.string.FRENCH, R.drawable.ic_language_french, FRENCH),
        Triple(R.string.INDIA, R.drawable.ic_language_hindi, INDIA),
        Triple(R.string.JAPANESE, R.drawable.ic_language_japan, JAPANESE),
        Triple(R.string.BRAZIL, R.drawable.ic_language_brazin, BRAZIL),
        Triple(R.string.VIETNAM, R.drawable.ic_language_vietnam, VIETNAM),
        Triple(R.string.KOREAN, R.drawable.ic_language_korea, KOREAN),
        Triple(R.string.TURKEY, R.drawable.ic_language_turkey, TURKEY),
        Triple(R.string.SPAIN, R.drawable.ic_language_spain, SPAIN),
        Triple(R.string.ITALIA, R.drawable.ic_language_italia, ITALIA),
        Triple(R.string.GERMAN, R.drawable.ic_language_german, GERMAN)
    )
    val supportLanguages1: MutableList<Triple<Int, Int, Locale>> = mutableListOf(
        Triple(R.string.FRENCH, R.drawable.ic_language_french, FRENCH),
        Triple(R.string.INDIA, R.drawable.ic_language_hindi, INDIA),
        Triple(R.string.JAPANESE, R.drawable.ic_language_japan, JAPANESE),
        Triple(R.string.BRAZIL, R.drawable.ic_language_brazin, BRAZIL),
        Triple(R.string.VIETNAM, R.drawable.ic_language_vietnam, VIETNAM),
        Triple(R.string.KOREAN, R.drawable.ic_language_korea, KOREAN),
        Triple(R.string.TURKEY, R.drawable.ic_language_turkey, TURKEY),
        Triple(R.string.SPAIN, R.drawable.ic_language_spain, SPAIN),
        Triple(R.string.ITALIA, R.drawable.ic_language_italia, ITALIA),
        Triple(R.string.GERMAN, R.drawable.ic_language_german, GERMAN),
        Triple(R.string.ENGLISH, R.drawable.ic_language_english, ENGLISH),
    )
}

val VIETNAM = Locale("vi")
val ITALIA = Locale("it")
val ENGLISH = Locale("en")
val JAPANESE = Locale("ja")
val KOREAN = Locale("ko")
val FRENCH = Locale("fr")
val GERMAN = Locale("de")
val BRAZIL = Locale("pt")
val SPAIN = Locale("es")
val INDIA = Locale("hi")
val TURKEY = Locale("tr")
val INDONESIA = Locale("in")
val ADS = Locale("ADS_LANGUAGE")


private const val SEPARATOR: String = "_"


fun supportedLanguages(): MutableList<Locale> = LocaleUtils.supportedLocales
fun supportedLanguages1(): MutableList<Locale> = LocaleUtils.supportedLocales1
fun supportDisplayLang(): MutableList<Triple<Int, Int, Locale>> = LocaleUtils.supportLanguages
fun supportDisplayLang1(): MutableList<Triple<Int, Int, Locale>> = LocaleUtils.supportLanguages1

fun defaultCountryCode(): String = LocaleUtils.defaultLocale.country

fun defaultLanguageTag(): String = toLanguageTag(LocaleUtils.defaultLocale)

fun toLanguageTag(locale: Locale): String = locale.language + SEPARATOR + locale.country

