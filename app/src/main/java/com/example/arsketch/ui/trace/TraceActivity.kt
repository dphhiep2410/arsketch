package com.example.arsketch.ui.trace

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivityTraceBinding
import com.example.arsketch.ui.dialog.DialogLoadOpenAds
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.arsketch.common.ImageProcessingUtils

class TraceActivity : BaseActivity<ActivityTraceBinding>(ActivityTraceBinding::inflate, { true }) {
    private var bitmap: Bitmap? = null
    private var isLock = false
    private var solidBitmap: Bitmap? = null
    private var hollowBitmap: Bitmap? = null
    private var configSolidBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null
    private var configHollowBitmap: Bitmap? = null
    private var isOrigin = true
    private var isSolid = false
    private var isHollow = false
    private var dialogLoadOpenAds: Dialog?= null
    private var currentBackgroundColor = -0x1


    override fun initView() {
        AdsCore.showNativeAds(this, binding.nativeAd, {
            binding.nativeAd.initCollapseEvent()
        }, {}, {}, NativeTypeEnum.DRAW)
        changeStatusBarColor(R.color.black)
        initBottomSheet()
        binding.bottomSheet.seekbarOpacity.progress = 10
        dialogLoadOpenAds = DialogLoadOpenAds().onCreateDialog(this)
        dialogLoadOpenAds?.show()
        initButton()
        val sketchModel = intent.getParcelableExtra<SketchModel>(Constant.KEY_SKETCH_MODEL)
        lifecycleScope.launch(Dispatchers.Default) {
            sketchModel?.let {
                if (it.localUrl != "") {
                    prepareFirstInit(0, it.localUrl)
                } else if (it.freeIdRawRes != 0) {
                    prepareFirstInit(it.freeIdRawRes, "")
                } else {
                    prepareFirstInit(0, it.remoteUrl)
                }
            }
        }

    }
    @SuppressLint("ClickableViewAccessibility")
    private fun prepareFirstInit(srcImage: Int, urlImage: String) {
        binding.bottomSheet.seekbarEdge.isEnabled = false
        if (srcImage == 0) {
            Glide.with(this)
                .asBitmap()
                .load(urlImage)
                .centerCrop().override(1080, 1080)
                .into(object : CustomTarget<Bitmap>() {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            originalBitmap = resource
                            solidBitmap = convertToSketchSolid(resource)
                            hollowBitmap = convertToSketchHollow(resource)
                            withContext(Dispatchers.Main) {
                                binding.photoView.setImageBitmap(originalBitmap)
                                binding.photoView.moveToCenter()
                                binding.photoView.setOnTouchListener { v, event ->
                                    if (!isLock) {
                                        binding.photoView.onTouch(
                                            v,
                                            event
                                        )
                                    } else {
                                        false
                                    }

                                }
                                dialogLoadOpenAds?.dismiss()
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Called when the resource is cleared
                    }
                })
        } else {
            Glide.with(this)
                .asBitmap()
                .load(srcImage)
                .centerCrop()
                .override(1080, 1080)
                .into(object : CustomTarget<Bitmap>() {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            originalBitmap = resource
                            solidBitmap = convertToSketchSolid(resource)
                            hollowBitmap = convertToSketchHollow(resource)
                            withContext(Dispatchers.Main) {
                                binding.photoView.setImageBitmap(originalBitmap)
                                binding.photoView.moveToCenter()
                                binding.photoView.setOnTouchListener { v, event ->
                                    if (!isLock) {
                                        binding.photoView.onTouch(
                                            v,
                                            event
                                        )
                                    } else {
                                        false
                                    }

                                }
                                dialogLoadOpenAds?.dismiss()
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }


    }
    private fun convertToSketchHollow(inputBitmap: Bitmap?): Bitmap {
        return ImageProcessingUtils.convertToSketchHollow(inputBitmap!!)
    }

    private fun convertToSketchSolid(inputBitmap: Bitmap?): Bitmap {
        return ImageProcessingUtils.convertToSketchSolid(inputBitmap!!)
    }

    private fun initButton() {
        binding.btnBack.clickWithDebounce {
            finish()
        }
        binding.btnToggleDirection.clickWithDebounce {
            if (isSolid) {
                configSolidBitmap = flipImage(configSolidBitmap!!)
                binding.photoView.setImageBitmap(configSolidBitmap)
            }
            if (isHollow) {
                configHollowBitmap = flipImage(configHollowBitmap!!)
                binding.photoView.setImageBitmap(configHollowBitmap)
            }

            if (isOrigin) {
                originalBitmap = flipImage(originalBitmap!!)
                binding.photoView.setImageBitmap(originalBitmap)
            }
        }

        binding.btnLock.clickWithDebounce {
            if (!isLock) {
                isLock = true
                lockView()
            } else {
                isLock = false
                unlockView()
            }
        }

        binding.bottomSheet.btnOriginal.clickWithDebounce {
            if (!isOrigin) {
                isOrigin = true
                isHollow = false
                isSolid = false
                binding.bottomSheet.btnOriginal.setBackgroundResource(R.drawable.bg_primary_50)
                binding.bottomSheet.btnSolid.setBackgroundResource(R.drawable.bg_primarylight_50)
                binding.bottomSheet.btnHollow.setBackgroundResource(R.drawable.bg_primarylight_50)
                binding.bottomSheet.btnOriginal.setTextColor(getColor(R.color.n1))
                binding.bottomSheet.btnSolid.setTextColor(getColor(R.color.n4))
                binding.bottomSheet.btnHollow.setTextColor(getColor(R.color.n4))

                binding.bottomSheet.seekbarEdge.alpha = .3f
                binding.bottomSheet.tvEdge.alpha = .3f
                binding.bottomSheet.seekbarEdge.isEnabled = false
                originalImage()
            }
        }

        binding.bottomSheet.btnSolid.clickWithDebounce {
            if (!isSolid) {
                isSolid = true
                isHollow = false
                isOrigin = false
                binding.bottomSheet.btnSolid.setBackgroundResource(R.drawable.bg_primary_50)
                binding.bottomSheet.btnOriginal.setBackgroundResource(R.drawable.bg_primarylight_50)
                binding.bottomSheet.btnHollow.setBackgroundResource(R.drawable.bg_primarylight_50)

                binding.bottomSheet.btnOriginal.setTextColor(getColor(R.color.n4))
                binding.bottomSheet.btnHollow.setTextColor(getColor(R.color.n4))
                binding.bottomSheet.btnSolid.setTextColor(getColor(R.color.n1))

                binding.bottomSheet.seekbarEdge.alpha = 1f
                binding.bottomSheet.tvEdge.alpha = 1f
                binding.bottomSheet.seekbarEdge.isEnabled = true

                solidImage()
            }
        }

        binding.bottomSheet.btnHollow.clickWithDebounce {
            if (!isHollow) {
                isHollow = true
                isSolid = false
                isOrigin = false
                binding.bottomSheet.btnHollow.setBackgroundResource(R.drawable.bg_primary_50)
                binding.bottomSheet.btnOriginal.setBackgroundResource(R.drawable.bg_primarylight_50)
                binding.bottomSheet.btnSolid.setBackgroundResource(R.drawable.bg_primarylight_50)

                binding.bottomSheet.btnOriginal.setTextColor(getColor(R.color.n4))
                binding.bottomSheet.btnSolid.setTextColor(getColor(R.color.n4))
                binding.bottomSheet.btnHollow.setTextColor(getColor(R.color.n1))

                binding.bottomSheet.seekbarEdge.alpha = 1f
                binding.bottomSheet.tvEdge.alpha = 1f
                binding.bottomSheet.seekbarEdge.isEnabled = true

                hollowImage()

            }
        }


        binding.bottomSheet.seekbarEdge.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isSolid) {
                    solidImage()
                }

                if (isHollow) {
                    hollowImage()
                }
            }
        })

        binding.bottomSheet.seekbarOpacity.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.photoView.alpha = binding.bottomSheet.seekbarOpacity.progress / 10f
            }

        })

        binding.btnColorPicker.clickWithDebounce {
            showDialogColor()
        }
    }
    private fun originalImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            dialogLoadOpenAds?.show()
            binding.photoView.setImageBitmap(originalBitmap)
            dialogLoadOpenAds?.dismiss()
        }
    }

    private fun solidImage() {
        dialogLoadOpenAds?.show()
        lifecycleScope.launch(Dispatchers.Default) {
            configSolidBitmap =
                adjustLineThickness(solidBitmap!!, binding.bottomSheet.seekbarEdge.progress + 1)
            withContext(Dispatchers.Main) {
                binding.photoView.setImageBitmap(configSolidBitmap)
                dialogLoadOpenAds?.dismiss()

            }

        }
    }
    private fun initBottomSheet() {
        val bottomSheet = BottomSheetBehavior.from(binding.bottomSheet.bts)
        bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }
    private fun hollowImage() {
        dialogLoadOpenAds?.show()
        lifecycleScope.launch(Dispatchers.Default) {
            configHollowBitmap =
                adjustLineThickness(hollowBitmap!!, binding.bottomSheet.seekbarEdge.progress + 1)
            withContext(Dispatchers.Main) {
                binding.photoView.setImageBitmap(configHollowBitmap)
                dialogLoadOpenAds?.dismiss()
            }

        }
    }
    private fun adjustLineThickness(inputBitmap: Bitmap, size: Int): Bitmap? {
        return ImageProcessingUtils.adjustLineThickness(inputBitmap, size)
    }

    private fun flipImage(mBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.setScale(-1f, 1f) // Horizontal flip matrix

        // If you want to recycle the original bitmap to free up memory
//        mBitmap.recycle()
        return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.width, mBitmap.height, matrix, true)
    }


    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(
                1080, 1080,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 1080, 1080)
        drawable.draw(canvas)
        return bitmap
    }
    private fun lockView() {
        with(binding) {
            btnBack.alpha = .3f
            btnToggleDirection.alpha = .3f
            btnColorPicker.alpha = .3f
            btnLock.setImageResource(R.drawable.ic_lock)
            btnBack.isEnabled = false
            btnColorPicker.isEnabled = false
            btnToggleDirection.isEnabled = false
        }
    }

    private fun unlockView() {
        with(binding) {
            btnBack.alpha = 1f
            btnToggleDirection.alpha = 1f
            btnColorPicker.alpha = 1f
            btnLock.setImageResource(R.drawable.ic_unlock)
            btnBack.isEnabled = true
            btnColorPicker.isEnabled = true
            btnToggleDirection.isEnabled = true
        }
    }

    private fun showDialogColor() {
        ColorPickerDialogBuilder.with(this).setTitle(getString(R.string.choose_color_background))
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12)
            .initialColor(currentBackgroundColor).setOnColorSelectedListener { selectedColor ->


            }.setPositiveButton(
                getString(R.string.custom_ok)
            ) { dialog, selectedColor, allColors ->
                changeBackgroundColor(selectedColor)
            }.setNegativeButton(
                getString(R.string.cancel)
            ) { dialog, which ->


            }.showColorEdit(true)
            .setColorEditTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_bright))
            .build().show()
    }

    private fun changeBackgroundColor(selectedColor: Int) {
        binding.backgroundMain.setBackgroundColor(selectedColor)
    }

    private fun replaceColor(src: Bitmap?): Bitmap? {
        if (src == null) return null
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        for (x in pixels.indices) {
            pixels[x] =
                (pixels[x] shl 8 and -0x1000000).inv() and Color.BLACK
        }

        return Bitmap.createBitmap(
            pixels,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    }

}