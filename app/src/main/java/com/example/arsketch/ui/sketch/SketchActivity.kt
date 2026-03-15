package com.example.arsketch.ui.sketch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.buildMinVersionT
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.common.toPx
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivitySketchBinding
import com.example.arsketch.ui.dialog.DialogLoadOpenAds
import com.example.arsketch.ui.dialog.DialogRequestRecordAudio
import com.example.arsketch.ui.dialog.DialogWatchAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.arsketch.common.ImageProcessingUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class SketchActivity : BaseActivity<ActivitySketchBinding>(ActivitySketchBinding::inflate,{true}) {
    private lateinit var cameraExecutor: ExecutorService
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var solidBitmap: Bitmap? = null
    private var hollowBitmap: Bitmap? = null
    private var configSolidBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null
    private var configHollowBitmap: Bitmap? = null
    private var isLock = false
    private var isOrigin = true
    private var isSolid = false
    private var isHollow = false
    private var dialogLoadOpenAds: Dialog? = null
    private var hbRecord: HBRecorder? = null
    private var action = { }

    override fun onPause() {
        super.onPause()
        if (hbRecord?.isBusyRecording == true) {
            try {
                hbRecord?.pauseScreenRecording()
            } catch (_: IllegalStateException) {
                // MediaRecorder may already be paused or in an invalid state
            }
        }
    }

    private fun startRecordingScreen() {
        if (!buildMinVersionT()) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                //do action
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
                launcher.launch(permissionIntent)
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    permissionRecord.launch(
                        Manifest.permission.RECORD_AUDIO
                    )
                } else {
                    val dialogRequestRecordAudio =
                        DialogRequestRecordAudio(this@SketchActivity, onPressAccept = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                            activityLauncher.launch(intent)
                        }, false)
                    dialogRequestRecordAudio.show()

                }
            }
        } else {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                action.invoke()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    notificationPermission.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    val dialogRequestRecordAudio =
                        DialogRequestRecordAudio(this@SketchActivity, onPressAccept = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                            notificationLauncherActivity.launch(intent)
                        }, true)
                    dialogRequestRecordAudio.show()

                }

            }
        }


    }

    private val permissionRecord =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                //do action
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
                launcher.launch(permissionIntent)
            }
        }

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
                launcher.launch(permissionIntent)
            }
        }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                //Start screen recording
                hbRecord?.startScreenRecording(it.data, it.resultCode)

            }
        }

    private val hbRecorderListener = object : HBRecorderListener {
        override fun HBRecorderOnStart() {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.btnRecord.setImageResource(R.drawable.ic_record)
                binding.btnRecord.clickWithDebounce {
                    stopRecording()
                }
            }
        }

        override fun HBRecorderOnComplete() {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.btnRecord.setImageResource(R.drawable.ic_record_inactive)
                binding.btnRecord.clickWithDebounce {
                    val dialogWatchAds = DialogWatchAds(this@SketchActivity, onClickWatch = {
//                        showRewardAds(action= {
                            startRecordingScreen()
//                        }, actionFailed = {},RewardTypeEnum.RECORD)
                    },true)
                    dialogWatchAds.show()
                }
            }
        }

        override fun HBRecorderOnError(errorCode: Int, reason: String?) {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.btnRecord.setImageResource(R.drawable.ic_record_inactive)
                binding.btnRecord.clickWithDebounce {
                    startRecordingScreen()
                }
            }
        }

        override fun HBRecorderOnPause() {
        }

        override fun HBRecorderOnResume() {
        }

    }

    private fun stopRecording() {
        hbRecord?.stopScreenRecording()
    }

    private val notificationLauncherActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                //do action
                action.invoke()
            }
        }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                //do action
                action.invoke()

            }
        }


    override fun initView() {
//        AdsCore.showBannerAds(this, binding.bannerAds, null, null,false, BannerTypeEnum.DRAW)
        AdsCore.showNativeAds(this,binding.nativeAd,{
            binding.nativeAd.initCollapseEvent(action = {
//                updateBottomSheetMargin(120)
            })
//            updateBottomSheetMargin(50)
        },{},{}, NativeTypeEnum.DRAW)
        hbRecord = HBRecorder(this, hbRecorderListener)
        hbRecord?.fileName = "Ar_recorder${System.currentTimeMillis()}"
        action = {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                //do action
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
                launcher.launch(permissionIntent)
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    permissionRecord.launch(
                        Manifest.permission.RECORD_AUDIO
                    )
                } else {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    activityLauncher.launch(intent)
                }
            }

        }
        changeStatusBarColor(R.color.black)
        cameraExecutor = Executors.newSingleThreadExecutor()
        initBottomSheet()
        initButton()
        dialogLoadOpenAds = DialogLoadOpenAds().onCreateDialog(this)
        dialogLoadOpenAds?.show()
        lifecycleScope.launch(Dispatchers.Default) {
            updateCameraUi()
        }
        binding.bottomSheet.seekbarOpacity.progress = 10

    }

    private fun setUpView() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.btnFlash.isEnabled = true
        } else {
            binding.btnFlash.isEnabled = false
            binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
            binding.btnFlash.alpha = .3f
        }

        if (!isLock) {
            unlockView()
        } else {
            lockView()

        }
    }

    private fun lockView() {
        with(binding) {
            btnBack.alpha = .3f
            btnFlash.alpha = .3f
            btnRecord.alpha = .3f
            btnToggleDirection.alpha = .3f
            btnLock.setImageResource(R.drawable.ic_lock)
            btnBack.isEnabled = false
            btnFlash.isEnabled = false
            btnRecord.isEnabled = false
            btnToggleDirection.isEnabled = false
        }
    }

    private fun unlockView() {
        with(binding) {
            btnBack.alpha = 1f
            btnFlash.alpha = 1f
            btnRecord.alpha = 1f
            btnToggleDirection.alpha = 1f
            btnLock.setImageResource(R.drawable.ic_unlock)
            btnBack.isEnabled = true
            btnFlash.isEnabled = true
            btnRecord.isEnabled = true
            btnToggleDirection.isEnabled = true
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
        binding.btnFlash.clickWithDebounce {
            if (!config.isTorchEnable) {
                camera?.cameraControl?.enableTorch(true)
                binding.btnFlash.setImageResource(R.drawable.ic_flash_on)
                config.isTorchEnable = true
            } else {
                camera?.cameraControl?.enableTorch(false)
                binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
                config.isTorchEnable = false
            }
        }
        binding.btnRecord.clickWithDebounce {
            val dialogWatchAds = DialogWatchAds(this, onClickWatch = {
//                showRewardAds(action= {
                    startRecordingScreen()
//                }, actionFailed = {},RewardTypeEnum.RECORD)
            },true)
            dialogWatchAds.show()

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
            OnSeekBarChangeListener {
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
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.photoView.alpha = binding.bottomSheet.seekbarOpacity.progress / 10f
            }

        })
    }

    override fun onResume() {
        super.onResume()
        binding.viewFinder.post {
            displayId = binding.viewFinder.display.displayId
            lifecycleScope.launch {
                setupCamera()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateCameraUi() {
        val sketchModel = intent.getParcelableExtra<SketchModel>(Constant.KEY_SKETCH_MODEL)
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

    private fun initBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bts)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

//    private fun updateBottomSheetMargin(pos:Int) {
//        binding.nativeAd.post {
//            val nativeAdHeight = if (binding.nativeAd.visibility == android.view.View.VISIBLE) {
//                binding.nativeAd.height
//            } else {
//                0
//            }
//            val extraSpacing = toPx(pos).toInt()
//            val totalBottomMargin = nativeAdHeight + extraSpacing
//
//            val params = binding.bottomSheet.bts.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
//            params.bottomMargin = totalBottomMargin
//            binding.bottomSheet.bts.layoutParams = params
//
//            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bts)
//            val currentState = bottomSheetBehavior.state
//            binding.bottomSheet.bts.requestLayout()
//            binding.bottomSheet.bts.post {
//                bottomSheetBehavior.state = currentState
//            }
//        }
//    }

    @SuppressLint("ClickableViewAccessibility")

    private fun prepareFirstInit(srcImage: Int, urlImage: String) {
        binding.bottomSheet.seekbarEdge.isEnabled = false
        if (srcImage == 0) {
            Glide.with(this)
                .asBitmap()
                .load(urlImage)
                .centerCrop()
                .override(1080, 1080)
                .into(object : CustomTarget<Bitmap>() {
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


    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(
                1080,
                1080,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 1080, 1080)
        drawable.draw(canvas)
        return bitmap
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

    private suspend fun setupCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(this).await()
        // Select lensFacing depending on the available cameras
        lensFacing = when {
            hasBackCamera() -> CameraSelector.LENS_FACING_BACK
            hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }
        bindCameraUseCases()


    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }
    private fun bindCameraUseCases() {
        val metrics = resources.displayMetrics
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = binding.viewFinder.display.rotation
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation).build()
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()
        if (camera != null) {
            // Must remove observers from the previous camera instance
            removeCameraStateObservers(camera!!.cameraInfo)
        }
        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
            setUpView()

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.removeObservers(this)
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(this) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                    }

                    CameraState.Type.OPENING -> {
                    }

                    CameraState.Type.OPEN -> {
                    }

                    CameraState.Type.CLOSING -> {
                    }

                    CameraState.Type.CLOSED -> {
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                    }

                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                    }

                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                    }

                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {

                    }
                }
            }
        }
    }

    private fun convertToSketchHollow(inputBitmap: Bitmap?): Bitmap {
        return ImageProcessingUtils.convertToSketchHollow(inputBitmap!!)
    }

    private fun convertToSketchSolid(inputBitmap: Bitmap?): Bitmap {
        return ImageProcessingUtils.convertToSketchSolid(inputBitmap!!)
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

    companion object {
        private const val TAG = "TAG"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hbRecord?.isBusyRecording == true) {
            hbRecord?.stopScreenRecording()
        }
    }
}