package com.example.arsketch.ui.draw

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.data.model.ColorModel
import com.example.arsketch.databinding.ActivityDrawBinding
import com.example.arsketch.ui.dialog.DialogCreateImage
import com.example.arsketch.ui.sketch.SketchActivity
import com.example.arsketch.ui.trace.TraceActivity
import com.hbisoft.hbrecorder.HBRecorderListener


class DrawActivity : BaseActivity<ActivityDrawBinding>(ActivityDrawBinding::inflate, { true }) {
    private var currentPenColor: String = "#FFB800"
    private var currentBackgroundColor: String = "#FFFFFF"
    private var adapterPenColor: AdapterColor? = null
    private var adapterBGColor: AdapterColor? = null


    private val hbRecorderListener = object : HBRecorderListener {
        override fun HBRecorderOnStart() {

        }

        override fun HBRecorderOnComplete() {
        }

        override fun HBRecorderOnError(errorCode: Int, reason: String?) {
        }

        override fun HBRecorderOnPause() {
        }

        override fun HBRecorderOnResume() {
        }

    }


    override fun initView() {
//        AdsCore.showBannerAds(this, binding.bannerAds, null,null, true, BannerTypeEnum.ONBOARD)
        AdsCore.showNativeAds(this, binding.nativeAdmob, {}, {}, {}, NativeTypeEnum.DRAW)
        with(binding) {
            seekBarPenWidth.show()
            seekbarEraserWidth.hide()
            rcvBgColor.hide()
            rcvPenColor.hide()
            paintView.strokeColor = getColor(R.color.primary)
            paintView.modeDraw(3f)
            seekBarPenWidth.progress = 2
            seekbarEraserWidth.progress = 2
            paintView.setBG(currentBackgroundColor)

        }


        binding.seekbarEraserWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.paintView.modeEraser(binding.seekbarEraserWidth.progress + 1f)
            }

        })

        binding.seekBarPenWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.paintView.strokeWidth = binding.seekBarPenWidth.progress + 1f
            }

        })
        initButton()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapterBGColor = AdapterColor(onClickItem = {
            currentBackgroundColor = it
            binding.paintView.setBG(it)
        })
        adapterBGColor?.submitData(Constant.listColor.map { ColorModel(it) })
        binding.rcvBgColor.adapter = adapterBGColor


        adapterPenColor = AdapterColor(onClickItem = {
            currentPenColor = it
            binding.paintView.strokeColor = Color.parseColor(currentPenColor)
        })
        adapterPenColor?.submitData(Constant.listColor.map { ColorModel(it) })
        binding.rcvPenColor.adapter = adapterPenColor

    }

    private fun initButton() {
        with(binding) {
            btnPen.clickWithDebounce {
                paintView.strokeColor = getColor(R.color.primary)
                paintView.modeDraw(seekBarPenWidth.progress + 1f)

                seekBarPenWidth.show()
                seekbarEraserWidth.hide()
                rcvBgColor.hide()
                rcvPenColor.hide()
                changeBackGroundColor(btnPen)
            }

            btnEraser.clickWithDebounce {
                paintView.strokeColor = getColor(R.color.transparent)
                paintView.modeEraser(seekbarEraserWidth.progress + 1f)

                seekBarPenWidth.hide()
                seekbarEraserWidth.show()
                rcvBgColor.hide()
                rcvPenColor.hide()
                changeBackGroundColor(btnEraser)

            }

            btnPenColor.clickWithDebounce {
                seekBarPenWidth.hide()
                seekbarEraserWidth.hide()
                rcvBgColor.hide()
                rcvPenColor.show()
                changeBackGroundColor(btnPenColor)

            }

            btnBGColor.clickWithDebounce {
                seekBarPenWidth.hide()
                seekbarEraserWidth.hide()
                rcvBgColor.show()
                rcvPenColor.hide()
                changeBackGroundColor(btnBGColor)

            }
            btnUndo.clickWithDebounce {
                paintView.undo()
            }

            btnForward.clickWithDebounce {
                paintView.forward()
            }

            btnSave.clickWithDebounce {
//                previewContainer.show()
//                preview.setImageDrawable(paintView.drawable)
                if (binding.paintView.getNumOfPath() == 0) {
                    Toast.makeText(
                        this@DrawActivity, getString(R.string.draw_something), Toast.LENGTH_SHORT
                    ).show()
                    return@clickWithDebounce
                }
                val dialogCreateImage = DialogCreateImage(
                    this@DrawActivity,
                    paintView.drawable!!,
                    onPressSketch = { it ->
                        val mIntent = Intent(this@DrawActivity, SketchActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)

                    },
                    onPressTrace = { it ->
                        val mIntent = Intent(this@DrawActivity, TraceActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)
                    }).onCreateDialog()
                dialogCreateImage.show()
            }

            binding.btnBack.clickWithDebounce {
                showInterAds(action = {
                    finish()
                }, actionFailed = {
                    finish()
                }, InterAdsEnum.BACK)
            }

        }
        changeBackPressCallBack {
            showInterAds(action = {
                finish()
            }, actionFailed = {
                finish()
            }, InterAdsEnum.BACK)
        }

    }

    private fun changeBackGroundColor(mView: TextView) {
        val listOfView =
            listOf(binding.btnPen, binding.btnPenColor, binding.btnEraser, binding.btnBGColor)
        listOfView.forEach {
            it.setTextColor(getColor(R.color.n3))
            for (drawable in it.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.n3), PorterDuff.Mode.SRC_IN
                    )
                }
            }

        }
        mView.setTextColor(getColor(R.color.primary))
        for (drawable in mView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_IN
                )
            }
        }

    }
}