package com.example.arsketch.ui.sketch

import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.arsketch.R
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.databinding.ActivityCameraBinding
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate,{true}) {


    override fun initView() {

    }




}