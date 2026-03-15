package com.example.arsketch.ui.sketch

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.arsketch.R
import com.example.arsketch.common.MediaStoreUt
import com.example.arsketch.common.base_component.BaseFragment
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.databinding.FragmentCameraBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraFragment : BaseFragment<FragmentCameraBinding>() {
    private lateinit var cameraExecutor: ExecutorService
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var displayId: Int = -1
    private var imageCapture: ImageCapture? = null
    private lateinit var mediaStoreUtils: MediaStoreUt

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCameraBinding {
        return FragmentCameraBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        changeStatusBarColor(R.color.black)
        mediaStoreUtils = MediaStoreUt(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()
        initButton()
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

    private fun initButton() {

        binding.btnClose.clickWithDebounce { requireActivity().finish() }
        binding.btnFlash.clickWithDebounce {
            if (!requireActivity().config.isTorchEnable) {
                camera?.cameraControl?.enableTorch(true)
                binding.btnFlash.setImageResource(R.drawable.ic_flash_on)
                requireActivity().config.isTorchEnable = true
            } else {
                camera?.cameraControl?.enableTorch(false)
                binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
                requireActivity().config.isTorchEnable = false
            }
        }


        binding.btnSwitch.clickWithDebounce {
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                if (hasFrontCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT
                    bindCameraUseCases()
                }

            } else {
                if (hasBackCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_BACK
                    bindCameraUseCases()
                }
            }
        }

        binding.btnCapture.clickWithDebounce {
            imageCapture?.let { imageCapture ->

                // Create time stamped name and MediaStore entry.
                val name = SimpleDateFormat(FILENAME, Locale.US)
                    .format(System.currentTimeMillis())
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        val appName = requireContext().resources.getString(R.string.app_name)
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
                    }
                }

                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions
                    .Builder(
                        requireContext().contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    .build()

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri
                            Log.d(TAG, "Photo capture succeeded: $savedUri")
                            lifecycleScope.launch {
                                if (mediaStoreUtils.getImages().isNotEmpty()) {
                                    val navController = Navigation.findNavController(
                                        requireActivity(),
                                        R.id.fragment_container
                                    )
                                    if (navController.currentDestination?.id == R.id.cameraFragment) {
                                        navController.navigate(
                                            CameraFragmentDirections.actionCameraFragmentToPhotoFragment(
                                                mediaStoreUtils.mediaStoreCollection.toString()
                                            )
                                        )
                                    }
                                }
                            }
                            // We can only change the foreground Drawable using API level 23+ API

                        }
                    })

                // We can only change the foreground Drawable using API level 23+ API

            }
        }
    }

    private suspend fun setupCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        // Select lensFacing depending on the available cameras
        lensFacing = when {
            hasBackCamera() -> CameraSelector.LENS_FACING_BACK
            hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }

        // Enable or disable switching between cameras
//        updateCameraSwitchButton()

        // Build and bind the camera use cases
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
            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
            setUpView()

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun setUpView() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.btnFlash.isEnabled = true
        } else {
            binding.btnFlash.isEnabled = false
            binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
            binding.btnFlash.alpha = .3f
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
                        // Ask the user to close other camera apps
//                        Toast.makeText(this,
//                            "CameraState: Pending Open",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
//                        Toast.makeText(this,
//                            "CameraState: Opening",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
//                        Toast.makeText(this,
//                            "CameraState: Open",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.Type.CLOSING -> {
                        // Close camera UI
//                        Toast.makeText(this,
//                            "CameraState: Closing",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.Type.CLOSED -> {
                        // Free camera resources
//                        Toast.makeText(this,
//                            "CameraState: Closed",
//                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
//                        Toast.makeText(this,
//                            "Stream config error",
//                            Toast.LENGTH_SHORT).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
//                        Toast.makeText(this,
//                            "Camera in use",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
//                        Toast.makeText(this,
//                            "Max cameras in use",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
//                        Toast.makeText(this,
//                            "Other recoverable error",
//                            Toast.LENGTH_SHORT).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
//                        Toast.makeText(this,
//                            "Camera disabled",
//                            Toast.LENGTH_SHORT).show()
                    }

                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
//                        Toast.makeText(this,
//                            "Fatal error",
//                            Toast.LENGTH_SHORT).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
//                        Toast.makeText(this,
//                            "Do not disturb mode enabled",
//                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun changeStatusBarColor(color: Int) {
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), color)
    }
    companion object {
        private const val TAG = "TAG"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_TYPE = "image/jpeg"
    }

}