package com.example.arsketch.ui.sketch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.buildMinVersionT
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.databinding.ActivityRequestPermissionBinding

class RequestPermissionActivity : BaseActivity<ActivityRequestPermissionBinding>(
    ActivityRequestPermissionBinding::inflate, { true }) {

    override fun initView() {
        AdsCore.showNativeAds(this, binding.nativeAdmob, {}, {}, {}, NativeTypeEnum.PERMISSION)
        changeStatusBarColor(R.color.white)
        val isCameraPermission = intent.getBooleanExtra(Constant.KEY_IS_CAMERA, true)
        if (isCameraPermission) {
            with(binding) {
                tvContent.text = getString(R.string.permission_content)
                imvIcon.setImageResource(R.drawable.ic_camera_per)
                switchPermission.clickWithDebounce {
                    if (switchPermission.isChecked) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            changeActiveStatus(true)
                        } else {
                            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                permissionCameraLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            } else {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                                activityCameraLauncher.launch(intent)
                            }
                        }

                    }
                }

            }
        } else {
            with(binding) {
                tvContent.text = getString(R.string.permission_content_1)
                imvIcon.setImageResource(R.drawable.ic_unlock)
                switchPermission.setOnClickListener {
                    if (switchPermission.isChecked) {
                        if (buildMinVersionT()) {
                            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                                changeActiveStatus(false)
                            } else {
                                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                                    permissionDataAccessLauncher.launch(
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    )
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                    activityDataAccessLauncher.launch(intent)
                                }
                            }
                        } else {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                changeActiveStatus(false)
                            } else {
                                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    permissionDataAccessLauncher.launch(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                    activityDataAccessLauncher.launch(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val permissionDataAccessLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (buildMinVersionT()) {
                if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    changeActiveStatus(false)
                } else {
                    binding.switchPermission.isChecked = false
                }
            } else {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    changeActiveStatus(false)

                } else {
                    binding.switchPermission.isChecked = false

                }
            }

        }

    private val activityDataAccessLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (buildMinVersionT()) {
                if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    changeActiveStatus(false)
                } else {
                    binding.switchPermission.isChecked = false
                }
            } else {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    changeActiveStatus(false)
                } else {
                    binding.switchPermission.isChecked = false
                }
            }
        }

    private val permissionCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                changeActiveStatus(true)
            }else{
                binding.switchPermission.isChecked = false
            }


        }

    private val activityCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                changeActiveStatus(true)
            }else{
                binding.switchPermission.isChecked = false
            }
        }

    private fun changeActiveStatus(isCamera: Boolean) {
        with(binding) {
            switchPermission.isChecked = true
            btnContinue.backgroundTintList = ColorStateList.valueOf(getColor(R.color.primary))
            btnContinue.setTextColor(getColor(R.color.white))
            btnContinue.isEnabled = true
            btnContinue.clickWithDebounce {
                if (isCamera) {
                    startActivity(
                        Intent(
                            this@RequestPermissionActivity,
                            SketchCategoryActivity::class.java
                        )
                    )
                    finish()
                } else {
                    startActivity(
                        Intent(
                            this@RequestPermissionActivity,
                            GalleryActivity::class.java
                        )
                    )
                    finish()
                }
            }
        }
    }


}