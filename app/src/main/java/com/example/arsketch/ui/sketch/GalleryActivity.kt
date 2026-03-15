package com.example.arsketch.ui.sketch

import android.content.Intent
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.hide
import com.example.arsketch.common.show
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivityGalleryBinding
import com.example.arsketch.ui.challenge.ChallengeActivity
import com.example.arsketch.ui.trace.TraceActivity
import com.example.arsketch.viewmodel.AppViewModel

class GalleryActivity : BaseActivity<ActivityGalleryBinding>(ActivityGalleryBinding::inflate,{true}) {
    private val viewModel by viewModels<AppViewModel>()
    private var adapterGallery: AdapterGallery? = null
    private var adapterGroup: AdapterGroup? = null
    private var isExpand = false

    override fun initView() {
        viewModel.getListGroupItem(this)
        viewModel.getAllImage(
            this,
        )
        initRecyclerView()
        observeData()
        initButton()
        changeStatusBarColor(R.color.white)
    }

    private fun initButton() {
        binding.btnBack.clickWithDebounce {
            finish()
        }

        binding.containerSelect.clickWithDebounce {
            if (!isExpand) {
                binding.imvArrow.rotation = 180f
                binding.containerGroup.show()

            }
        }
    }

    private fun initRecyclerView() {
        adapterGallery = AdapterGallery {
            val sketchModel =
                SketchModel(
                    id = 0,
                    remoteUrl = "",
                    freeIdRawRes = 0,
                    localUrl = it.imageUri,
                    isFree = true,
                    isUnlocked = true
                )
            if(MainApplication.app.isChallenge){
                val mIntent = Intent(this, ChallengeActivity::class.java)
                mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                startActivity(mIntent)
            }else{
                if(MainApplication.app.isSketch){
                    val mIntent = Intent(this, SketchActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                    startActivity(mIntent)
                }else{
                    val mIntent = Intent(this, TraceActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, sketchModel)
                    startActivity(mIntent)
                }
            }


        }

        val gridLayoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rcvImage.layoutManager = gridLayoutManager

        binding.rcvImage.adapter = adapterGallery


        adapterGroup = AdapterGroup {
            binding.containerGroup.hide()
            binding.imvArrow.rotation = 0f
            binding.tvTitle.text = it.name
            viewModel.getImageFromFolder(
                this, it.folderUri
            )
        }
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvGroup.layoutManager = linearLayoutManager

        binding.rcvGroup.adapter = adapterGroup

    }

    private fun observeData() {
        viewModel.listGroupImage.observe(this) {
            it?.let {
                adapterGroup?.setData(it)
                if (it.isEmpty()) {
                    binding.containerEmptyImage.show()
                } else {
                    binding.containerEmptyImage.hide()
                }
            }
        }

        viewModel.listImageModel.observe(this) {
            it?.let {
                adapterGallery?.setData(it)
                if (it.isEmpty()) {
                    binding.containerEmptyImage.show()
                } else {
                    binding.containerEmptyImage.hide()
                }
            }
        }
    }
}