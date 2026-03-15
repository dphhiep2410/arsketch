package com.example.arsketch.ui.sketch

import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.DataCategory
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivitySketchCategoryBinding
import com.example.arsketch.ui.challenge.ChallengeActivity
import com.example.arsketch.ui.trace.TraceActivity

class SketchCategoryActivity : BaseActivity<ActivitySketchCategoryBinding>(
    ActivitySketchCategoryBinding::inflate,{true}
) {
    private var adapterSketchCategory: AdapterSketchCategory? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val sketchModel = SketchModel(
                id = 0,
                remoteUrl = "",
                freeIdRawRes = 0,
                localUrl = uri.toString(),
                isFree = true,
                isUnlocked = true
            )
            if (MainApplication.app.isChallenge) {
                val mIntent = Intent(this, ChallengeActivity::class.java)
                mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                startActivity(mIntent)
            } else {
                if (MainApplication.app.isSketch) {
                    val mIntent = Intent(this, SketchActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                    startActivity(mIntent)
                } else {
                    val mIntent = Intent(this, TraceActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                    startActivity(mIntent)
                }
            }
        }
    }

    override fun initView() {
        AdsCore.showNativeAds(this,binding.nativeAdmob,{},{},{}, NativeTypeEnum.CATEGORY_2)
        changeStatusBarColor(R.color.white)
        initRecyclerView()
        initButton()
        if(MainApplication.app.isChallenge){
            binding.tvTitle.text = getString(R.string.draw_challenge)
        }else{
            if (MainApplication.app.isSketch) {
                binding.tvTitle.text = getString(R.string.draw_sketch)
            } else {
                binding.tvTitle.text = getString(R.string.draw_trace)
            }
        }

    }

    private fun initButton() {
        binding.btnBack.clickWithDebounce {
            showInterAds(action = {
                finish()
            }, actionFailed = {
                finish()
            }, InterAdsEnum.BACK)
        }

        changeBackPressCallBack {
            showInterAds(action = {
                finish()
            }, actionFailed = {
                finish()
            }, InterAdsEnum.BACK)
        }
        binding.containerCamera.clickWithDebounce {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.containerGallery.clickWithDebounce {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun initRecyclerView() {
        val listData = mutableListOf(
            DataCategory(0, getString(R.string.animal), R.drawable.ic_category_1),
            DataCategory(1, getString(R.string.chibi), R.drawable.ic_category_2),
            DataCategory(2, getString(R.string.flower), R.drawable.ic_category_3),
            DataCategory(3, getString(R.string.forkid), R.drawable.ic_category_4),
            DataCategory(4, getString(R.string.learn_to_draw), R.drawable.ic_category_5),
            DataCategory(5, getString(R.string.manga), R.drawable.ic_category_6),
            DataCategory(6, getString(R.string.nature), R.drawable.ic_category_7),
            DataCategory(7, getString(R.string.people), R.drawable.ic_category_8),
            DataCategory(8, getString(R.string.christmas), R.drawable.ic_category_9),
            DataCategory(9, getString(R.string.cute), R.drawable.ic_category_10),
            DataCategory(10, getString(R.string.face), R.drawable.ic_category_11),
            DataCategory(11, getString(R.string.food), R.drawable.ic_category_12),
            DataCategory(12, getString(R.string.tattoo), R.drawable.ic_category_13),
            DataCategory(13, getString(R.string.vegetable), R.drawable.ic_category_14),
            DataCategory(14, getString(R.string.vehicle), R.drawable.ic_category_15),
            DataCategory(15, getString(R.string.halloween), R.drawable.halloween1),
            DataCategory(16, getString(R.string.tet_holiday), R.drawable.tet_1),
            DataCategory(17, getString(R.string.valentine), R.drawable.valentine_1),
        )
        adapterSketchCategory = AdapterSketchCategory(listData, onClickItem = {
            val mIntent = Intent(this, ListSketchDataActivity::class.java)
            mIntent.putExtra(Constant.KEY_SKETCH_CATEGORY, it)
            startActivity(mIntent)
        })
        val gridLayoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rcvCategory.layoutManager = gridLayoutManager
        binding.rcvCategory.adapter = adapterSketchCategory
        loadAds()
    }

    private fun loadAds() {

    }

}
