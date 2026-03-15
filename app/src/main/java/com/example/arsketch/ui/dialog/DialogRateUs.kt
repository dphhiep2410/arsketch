package com.example.arsketch.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.databinding.DialogRateUsBinding
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode


class DialogRateUs(private val activity: Activity, private val onPressPositive:()->Unit,private val onNegative:()->Unit) {
    private lateinit var binding: DialogRateUsBinding
    private var star = 0
    fun onCreateDialog(): Dialog {
        binding = DialogRateUsBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(activity.getColor(R.color.transparent)))
        dialog.window!!.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        dialog.window!!.setLayout(
            (activity.resources.displayMetrics.widthPixels),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        initView(dialog)
        return dialog
    }

    private fun initView(dialog: Dialog) {
        initFirst(dialog)
        initButton(dialog)

    }

    private fun initButton(dialog: Dialog) {
        binding.btnRate.clickWithDebounce {
            if (star == 0) {
                Toast.makeText(
                    activity, activity.getString(R.string.please_rate), Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isInternetAvailable(activity)) {
                   activity.config.isUserRated = true

                    val manager = ReviewManagerFactory.create(activity)
                    manager.requestReviewFlow().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            manager.launchReviewFlow(activity, reviewInfo)
                        } else {
                            @ReviewErrorCode val reviewErrorCode =
                                (task.exception as ReviewException).errorCode
                        }
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        activity, activity.getString(R.string.internet_not_available), Toast.LENGTH_SHORT
                    ).show()
                }
                onPressPositive.invoke()
                dialog.dismiss()

            }
        }
        binding.root.clickWithDebounce {

        }
        binding.btnLater.clickWithDebounce {
            activity.config.rateTime = System.currentTimeMillis()

//            AppSharePreference.INSTANCE.saveTimeToShowRate(time)
            onNegative.invoke()
            dialog.dismiss()
        }

    }


    private fun initFirst(dialog: Dialog) {
        binding.containerMain.clickWithDebounce {}

        val groupImageStatus = listOf(
            R.drawable.ic_status_1,
            R.drawable.ic_status_2,
            R.drawable.ic_status_3,
            R.drawable.ic_status_4,
            R.drawable.ic_status_5,
        )
        val groupStar =
            listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

        groupStar.forEachIndexed { index, item ->
            kotlin.run {
                item.clickWithDebounce {
                    star = index + 1
                    binding.btnLater.visibility = View.GONE
                    binding.imvStatus.setImageResource(groupImageStatus[index])
                    when (star) {
                        in 1..3 -> {
                            binding.btnRate.text = activity.getString(R.string.rate_us_button)
                        }

                        4 -> {
                            binding.btnRate.text = activity.getString(R.string.rate_us_button)
                        }

                        else -> {
                            binding.btnRate.text = activity.getString(R.string.rate_on_google_play)
                        }
                    }
                    binding.tvStatus1.visibility = View.VISIBLE

                    val subStar = groupStar.slice(0..index)
                    if (index < groupStar.size - 1) {
                        val subStarInActive = groupStar.slice(index + 1..4)
                        subStarInActive.forEachIndexed { _, item ->
                            kotlin.run {
                                if (item.id != R.id.star_5) {
                                    item.setImageResource(
                                        R.drawable.ic_star_inactive
                                    )
                                } else {
                                    item.setImageResource(
                                        R.drawable.ic_last_star_inactive
                                    )
                                }

                            }
                        }
                    }
                    subStar.forEachIndexed { index, item ->
                        kotlin.run {
                            if (index == 4) {
                                item.setImageResource(R.drawable.ic_last_star_active)
                            } else {
                                item.setImageResource(
                                    R.drawable.ic_star_active
                                )
                            }

                        }
                    }
                }
            }
        }
    }

}