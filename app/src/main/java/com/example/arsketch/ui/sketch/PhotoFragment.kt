package com.example.arsketch.ui.sketch

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.MediaStoreFile
import com.example.arsketch.common.MediaStoreUt
import com.example.arsketch.common.base_component.BaseFragment
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.FragmentPhotoBinding
import com.example.arsketch.ui.challenge.ChallengeActivity
import com.example.arsketch.ui.trace.TraceActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch


class PhotoFragment : BaseFragment<FragmentPhotoBinding>() {

    private var mediaList: MutableList<MediaStoreFile> = mutableListOf()
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPhotoBinding {
        return FragmentPhotoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        changeStatusBarColor(R.color.black)
        lifecycleScope.launch {
            // Get images this app has access to from MediaStore
            mediaList = MediaStoreUt(requireContext()).getImages()
            Glide.with(requireActivity()).load(mediaList[0].uri).into(binding.image)
        }
        initButton()
    }

    private fun initButton() {
        binding.btnClose.clickWithDebounce {
            findNavController().popBackStack()
        }

        binding.btnCheck.clickWithDebounce {
            val sketchModel = SketchModel(0,mediaList[0].uri.toString(),"",0,true,true)

            if(MainApplication.app.isChallenge){
                val mIntent = Intent(requireContext(),ChallengeActivity::class.java)
                mIntent.putExtra(Constant.KEY_SKETCH_MODEL,sketchModel)
                startActivity(mIntent)
                requireActivity().finish()
            }else{
                if(MainApplication.app.isSketch){
                    val mIntent = Intent(requireContext(),SketchActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL,sketchModel)
                    startActivity(mIntent)
                    requireActivity().finish()
                }else{
                    val mIntent = Intent(requireContext(),TraceActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL,sketchModel)
                    startActivity(mIntent)
                    requireActivity().finish()
                }
            }

        }
    }

    private fun changeStatusBarColor(color: Int) {
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), color)
    }

}