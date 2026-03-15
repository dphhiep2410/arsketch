package com.example.arsketch.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsketch.MainApplication
import com.example.arsketch.common.Constant
import com.example.arsketch.common.MediaStoreUtils
import com.example.arsketch.data.model.GroupImageModel
import com.example.arsketch.data.model.ImageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class AppViewModel : ViewModel() {
    var dataSource = MutableLiveData<Map<String, List<String>>>()
    private val _listItemListGroupFile = MutableLiveData<MutableList<GroupImageModel>?>()
    fun setListGroupImage(list: MutableList<GroupImageModel>?) {
        _listItemListGroupFile.postValue(list)
    }

    val listGroupImage: LiveData<MutableList<GroupImageModel>?> get() = _listItemListGroupFile


    private val _listImage = MutableLiveData<MutableList<ImageModel>?>()
    val listImageModel: LiveData<MutableList<ImageModel>?> get() = _listImage
    fun setListImage(list: MutableList<ImageModel>?) {
        _listImage.postValue(list)
    }

    fun getListDataSource() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                    .url(Constant.keyData)
                    .build()
                val responses: Response = client.newCall(request).execute()
//                val apiResponse = URL(Constant.keyData).readText()
//                Log.d("TAG", "getListDataSource: "+apiResponse)
                val data = jsonStringToMap(responses.body?.string().toString()).toMutableMap()
                dataSource.postValue(data)
                MainApplication.app.dataSource.postValue(data)

            } catch (e: Exception) {
                e.printStackTrace()
                dataSource.postValue(mutableMapOf())
            }

        }
    }

    private fun jsonStringToMap(jsonString: String): Map<String, List<String>> {
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, List<String>>()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.get(key)

                if (value is JSONArray) {
                    val list = mutableListOf<String>()
                    for (i in 0 until value.length() - 1) {
                        list.add(value.getString(i))
                    }
                    map[key] = list
                }
            }
            Log.d("TAG", "jsonStringToMap: " + map)
            return map
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableMapOf()
        }

    }

    fun getListGroupItem(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            setListGroupImage(MediaStoreUtils.getListGroupItem(context).toMutableList())
        }
    }

    fun getImageFromFolder(
        context: Context, folderPath: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            setListImage(MediaStoreUtils.getChildImageFromPath(context, folderPath))
        }
    }

    fun getAllImage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TAG", "getAllImage: "+MediaStoreUtils.getAllImage(context).toMutableList())
            setListImage(MediaStoreUtils.getAllImage(context).toMutableList())
        }
    }

}