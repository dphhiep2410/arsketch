package com.example.arsketch.ui.finish

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.arsketch.common.Constant
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivityFinishBinding
import com.example.arsketch.ui.main.MainActivity
import com.example.arsketch.ui.sketch.SketchCategoryActivity
import java.io.File


class FinishActivity : BaseActivity<ActivityFinishBinding>(ActivityFinishBinding::inflate,{true}) {

    override fun initView() {
        binding.btnBack.clickWithDebounce {
            startActivity(Intent(this, SketchCategoryActivity::class.java))
            finish()
        }

        binding.btnHome.clickWithDebounce {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        val sketchModel =
            intent.getParcelableExtra<SketchModel>(Constant.KEY_SKETCH_MODEL)

        val imageUri = intent.getParcelableExtra<Uri>(Constant.KEY_IMAGE_DRAW)
        sketchModel?.let {
            if (it.localUrl != "") {
                Glide.with(this).load(it.localUrl).into(binding.imageSample)

            } else if (it.freeIdRawRes != 0) {
                Glide.with(this).load(it.freeIdRawRes).into(binding.imageSample)
            } else {
                Glide.with(this).load(it.remoteUrl).into(binding.imageSample)
            }
        }
        imageUri?.let {
            binding.imageArt.setImageURI(it)
            binding.btnShare.clickWithDebounce {
                val imageFile = File(imageUri.path)
                val contentUri = FileProvider.getUriForFile(
                    this,
                    packageName + ".provider",
                    imageFile
                )
                val shareIntent = Intent()
                shareIntent.setAction(Intent.ACTION_SEND)
                shareIntent.setType("image/*")
                // Add the content URI to the intent

                // Add the content URI to the intent
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)

                // Grant temporary read permission to the content URI

                // Grant temporary read permission to the content URI
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Start the share activity

                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Share Image"))


            }
        }
    }


}