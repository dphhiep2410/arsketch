package com.example.arsketch.ui.language

import android.content.Intent
import android.view.View
import android.widget.ScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.common.supportDisplayLang1
import com.example.arsketch.common.supportedLanguages
import com.example.arsketch.databinding.ActivityLanguageBinding
import com.example.arsketch.ui.main.MainActivity
import com.example.arsketch.ui.onboard.OnboardActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate, { true }) {

    private var currentLanguage = Locale.getDefault().language
    private var adapter: AdapterLanguage? = null
    private var currentIndex = -1
    private var applyButtonJob: Job? = null

    override fun initView() {
        // Pre-load native ads ngay khi vào màn, chưa hiển thị
        AdsCore.showNativeAds(this, binding.nativeAdmob, {}, {}, {}, NativeTypeEnum.LANG)
        binding.containerAds.visibility = View.GONE
        binding.containerButton.hide()

        currentLanguage = config.savedLanguage
        if (currentLanguage !in supportedLanguages().map { it.language }) {
            currentLanguage = "en"
            config.savedLanguage = "en"
        }
        if (intent.getBooleanExtra(Constant.KEY_FROM_MAIN, false)) {
            binding.btnBack.show()
            binding.btnBack.clickWithDebounce { finish() }
        } else {
            binding.btnBack.hide()
        }
        changeBackPressCallBack {
            if (intent.getBooleanExtra(Constant.KEY_FROM_MAIN, false)) {
                finish()
            }
        }
        initRecyclerView()
        initButton()
    }

    // Gọi khi user chọn 1 option bất kỳ
    private fun onLanguageSelected() {
        // Show native ads container (đã pre-load sẵn)
        binding.containerAds.visibility = View.VISIBLE

        // Chỉ bắt đầu đếm 1 lần duy nhất
        if (applyButtonJob != null) return
        binding.loadingAds.show()
        applyButtonJob = lifecycleScope.launch {
            delay(3000)
            binding.containerButton.show()
            binding.loadingAds.hide()
        }
        binding.root.post {
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun initButton() {
        binding.containerButton.clickWithDebounce {
            if (currentIndex == -1) {
                toast(R.string.selected_lang)
            } else {
                clickApplyBtn()
            }
        }

        binding.containerCurrentLanguage.clickWithDebounce {
            currentIndex = 100
            currentLanguage = config.savedLanguage
            binding.tick.setImageResource(R.drawable.ic_tick_active)
            binding.tvCountry.setTextColor(getColor(R.color.black))
            binding.containerCurrentLanguage.setBackgroundResource(R.drawable.bg_lang_selected)
            adapter?.removeSelectedId()
            onLanguageSelected()
        }
    }

    private fun clickApplyBtn() {
        config.savedLanguage = currentLanguage
        if (!intent.getBooleanExtra(Constant.KEY_FROM_MAIN, false)) {
            startActivity(Intent(this, OnboardActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    private fun initRecyclerView() {
        val mDisplayLangList: MutableList<Triple<Int, Int, Locale>> =
            supportDisplayLang1().toMutableList()
        adapter = AdapterLanguage(this, onCLickItem = {
            currentIndex = 100
            currentLanguage = it.third.language
            binding.tick.setImageResource(R.drawable.ic_tick_off)
            binding.tvCountry.setTextColor(getColor(R.color.black))
            binding.containerCurrentLanguage.setBackgroundResource(R.drawable.bg_lang_unselected)
            onLanguageSelected()
        })

        adapter?.setData(mDisplayLangList)
        handleUnSupportLang(mDisplayLangList)

        binding.rcvLanguage.adapter = adapter
        binding.rcvLanguage.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun handleUnSupportLang(mLanguageList: MutableList<Triple<Int, Int, Locale>>) {
        var support = false
        mLanguageList.forEachIndexed { index, item ->
            if (item.third.language == currentLanguage) {
                support = true
                binding.tick.setImageResource(R.drawable.ic_tick_off)
                binding.imvFlag.setImageResource(item.second)
                binding.tvCountry.text = getText(item.first)
                val newDisplayLangList =
                    mLanguageList.filter { l -> l.third.language != item.third.language }
                        .toMutableList()
                adapter?.setData(newDisplayLangList)
            }
        }
        if (!support) {
            currentLanguage = mLanguageList[0].third.language
            val newDisplayLangList =
                mLanguageList.filter { l -> l.third.language != mLanguageList[0].third.language }
                    .toMutableList()
            adapter?.setData(newDisplayLangList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        applyButtonJob?.cancel()
    }
}
