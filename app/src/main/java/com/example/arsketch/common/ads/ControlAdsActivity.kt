package com.example.arsketch.common.ads

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dream.libads.utils.AdsConfigUtils
import com.dream.libads.utils.Constants
import com.example.arsketch.BuildConfig
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.common.hide
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.common.show
import com.example.arsketch.databinding.ActivityControlAdsBinding
import com.example.arsketch.ui.splash.SplashActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class ControlAdsActivity : AppCompatActivity() {
    private var handler: Handler? = Handler()
    private var runnable: Runnable? = null

    private var adapterAds: AdapterAds? = null
    private val binding: ActivityControlAdsBinding by lazy {
        ActivityControlAdsBinding.inflate(layoutInflater)
    }
    private var listItemConfig = mutableListOf<ItemAds>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == "staging") {
            if (config.isAdsConfigDone) {
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
                return
            }
            setContentView(binding.root)
            binding.loading.show()
            binding.rcv.hide()
            initRecyclerView()
            fetchRemoteConfig()

        } else {
            config.canSplashLoadConfig = true
            config.isRealAds = BuildConfig.FLAVOR == "production"
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }
    }

    private fun initRecyclerView() {
        adapterAds = AdapterAds(onChange = { key, value ->
            Log.d("TAG", "key:value-> $key = $value")
            val newList = listItemConfig.toMutableList()
            val item = newList.find { it.key == key }!!
            if (item.value == 0) {
                item.value = 1
            } else if(item.value == 1) {
                item.value = 0
            }

            adapterAds?.setListConfig(newList)
            AdsConfigUtils(this).putValue(key, item.value)

        })

        binding.rcv.setHasTransientState(false)
        binding.rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcv.adapter = adapterAds
    }

    private fun fetchRemoteConfig() {
        binding.check.isChecked = config.canSplashLoadConfig
        binding.check.setOnCheckedChangeListener { _, isChecked ->
            config.canSplashLoadConfig = isChecked
        }

        binding.btnNext.clickWithDebounce {
            config.isAdsConfigDone = true
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        binding.check2.isChecked = config.isRealAds
        binding.check2.setOnCheckedChangeListener { _, isChecked ->
            config.isRealAds = isChecked
        }

        binding.check3.setOnClickListener {
            if (binding.check3.isChecked) {
                binding.check4.isChecked = false
                val newList = listItemConfig.map { it.copy(value = 0) }
                adapterAds?.setListConfig(newList)
                newList.forEach {
                    AdsConfigUtils(this).putValue(it.key, it.value)
                }
                val value = FirebaseRemoteConfig.getInstance().getLong("time_show_ads")
                AdsConfigUtils(this).putValue("time_show_ads", value.toInt())
                val valueTimeMrec = FirebaseRemoteConfig.getInstance().getLong("time_show_banner")
                AdsConfigUtils(this).putValue("time_show_banner", valueTimeMrec.toInt())

                val valueTimeReload = FirebaseRemoteConfig.getInstance().getLong("time_reload_native")
                AdsConfigUtils(this).putValue("time_reload_native", valueTimeReload.toInt())
                config.customConfig = true
            } else {
                adapterAds?.setListConfig(listItemConfig)
                config.customConfig = false

            }
        }

        binding.check4.setOnClickListener {
            if (binding.check4.isChecked) {
                binding.check3.isChecked = false
                val newList = listItemConfig.map { if(it.value == 0 || it.value == 1) it.copy(value = 1) else it }
                adapterAds?.setListConfig(newList)
                newList.forEach {
                    AdsConfigUtils(this).putValue(it.key, it.value)
                }
                val value = FirebaseRemoteConfig.getInstance().getLong("time_show_ads")
                AdsConfigUtils(this).putValue("time_show_ads", value.toInt())
                val valueTimeMrec = FirebaseRemoteConfig.getInstance().getLong("time_show_banner")
                AdsConfigUtils(this).putValue("time_show_banner", valueTimeMrec.toInt())

                val valueTimeReload = FirebaseRemoteConfig.getInstance().getLong("time_reload_native")
                AdsConfigUtils(this).putValue("time_reload_native", valueTimeReload.toInt())
                config.customConfig = true
            } else {
                adapterAds?.setListConfig(listItemConfig)
                config.customConfig = false

            }
        }

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        handler = Handler()
        runnable = Runnable {
            assignValueFromFireBase()
            handler = null
        }
        if (isInternetAvailable(this)) {
            handler?.postDelayed(runnable!!, Constants.TIME_OUT_DELAY)

            mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
                if (handler == null) {
                    return@addOnCompleteListener
                }
                handler?.removeCallbacks(runnable!!)
                handler = null
                assignValueFromFireBase()

            }
        } else {
            assignValueFromFireBase()
        }

    }

    private fun assignValueFromFireBase() {
        val newList = mutableListOf<ItemAds>()

        AdsConfigUtils(this).listKey.forEach {
            val value = FirebaseRemoteConfig.getInstance().getLong(it)
            listItemConfig.add(ItemAds(it, value.toInt()))
            newList.add(ItemAds(it, value.toInt()))
            AdsConfigUtils(this).putValue(it, value.toInt())
        }
        adapterAds?.setListConfig(newList)
        binding.loading.hide()
        binding.rcv.show()
        val value = FirebaseRemoteConfig.getInstance().getLong("time_show_ads")
        AdsConfigUtils(this).putValue("time_show_ads", value.toInt())
        val valueTimeMrec = FirebaseRemoteConfig.getInstance().getLong("time_show_banner")
        AdsConfigUtils(this).putValue("time_show_banner", valueTimeMrec.toInt())

        val valueTimeReload = FirebaseRemoteConfig.getInstance().getLong("time_reload_native")
        AdsConfigUtils(this).putValue("time_reload_native", valueTimeReload.toInt())
//        Constants.TIME_OUT = AdsConfigUtils(this).getDefTimeShowAds().toLong()
//        Constants.TIME_OUT = 0
    }
}
