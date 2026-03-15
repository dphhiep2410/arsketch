package com.example.arsketch.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    val editor: SharedPreferences.Editor = edit()
    editor.func()
    editor.apply()
}


class AppSharePreference(private val context: Context?) {
    companion object {
        lateinit var INSTANCE: AppSharePreference

        @JvmStatic
        fun getInstance(context: Context?): AppSharePreference {
            if (!Companion::INSTANCE.isInitialized) {
                INSTANCE = AppSharePreference(context)
            }
            return INSTANCE
        }

    }


    fun saveIsSetLangFirst(values: Boolean) {
        saveBoolean(Constant.KEY_SET_LANG, values)
    }

    fun getSetLangFirst(defaultValues: Boolean): Boolean {
        return getBoolean(Constant.KEY_SET_LANG, defaultValues)
    }


     fun saveLong(key: String, values: Long) = sharedPreferences().edit {
        putLong(key, values)
    }

     fun getLong(key: String, defaultValues: Long): Long {
        return try {
            sharedPreferences().getLong(key, defaultValues)
        } catch (e: Exception) {
            sharedPreferences().edit { putLong(key, defaultValues) }
            defaultValues
        }
    }




     fun saveInt(key: String, values: Int) = sharedPreferences().edit {
        putInt(key, values)
    }

     fun getInt(key: String, defaultValues: Int): Int {
        return try {
            sharedPreferences().getInt(key, defaultValues)
        } catch (e: Exception) {
            sharedPreferences().edit { putInt(key, defaultValues) }
            defaultValues
        }
     }

    fun saveString(key: String, values: String): Unit =
        sharedPreferences().edit { putString(key, values) }

    fun getString(key: String, defaultValues: String): String {
        return try {
            sharedPreferences().getString(key, defaultValues)!!
        } catch (e: Exception) {
            sharedPreferences().edit { putString(key, defaultValues) }
            defaultValues
        }
    }

    fun saveMap(key: String, values: MutableMap<Int, List<Pair<Int,String>>>) {
        val gson = Gson()
        sharedPreferences().edit { putString(key, gson.toJson(values)) }
    }

    fun getMap(key: String, defaultValues: MutableMap<Int, List<Pair<Int,String>>>): MutableMap<Int, List<Pair<Int,String>>> {
        val gson = Gson()
        val type = object : TypeToken<MutableMap<Int, List<Pair<Int,String>>>>() {}.type
        return try {
            val returnString = sharedPreferences().getString(key, gson.toJson(defaultValues))
            gson.fromJson(returnString, type)
        } catch (e: Exception) {
            sharedPreferences().edit { putString(key, gson.toJson(defaultValues)) }
            defaultValues
        }
    }

     fun saveBoolean(key: String, values: Boolean) {
        sharedPreferences().edit { putBoolean(key, values) }
    }

     fun getBoolean(key: String, defaultValues: Boolean): Boolean {
        return try {
            sharedPreferences().getBoolean(key, defaultValues)
        } catch (e: Exception) {
            sharedPreferences().edit { putBoolean(key, defaultValues) }
            defaultValues
        }
    }

     fun saveStringSet(key: String, values: HashSet<String>) {
        sharedPreferences().edit { putStringSet(key, values) }
    }

     fun getStringSet(key: String, defaultValues: HashSet<String>): HashSet<String> {
        return try {
            sharedPreferences().getStringSet(key, defaultValues)!! as HashSet
        } catch (e: Exception) {
            sharedPreferences().edit { putStringSet(key, defaultValues) }
            defaultValues
        }
    }

    fun registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener): Unit =
        sharedPreferences().registerOnSharedPreferenceChangeListener(
            onSharedPreferenceChangeListener
        )

    private fun defaultSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun sharedPreferences(): SharedPreferences = defaultSharedPreferences(context!!)



    inline fun <reified T> saveObjectToSharePreference(key: String, data: T) {
        val gson = Gson()
        val json = gson.toJson(data)
        sharedPreferences().edit().putString(key, json).apply()
    }

    inline fun <reified T> getObjectFromSharePreference(key: String): T? {
        val serializedObject: String? = sharedPreferences().getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<T?>() {}.type
            return gson.fromJson(serializedObject, type)
        }
        return null
    }
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

}

