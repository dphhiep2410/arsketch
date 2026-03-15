package com.example.arsketch.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.example.arsketch.BuildConfig
import com.example.arsketch.R
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.DialogCreateImageBinding
import java.io.File
import java.io.FileOutputStream


class DialogCreateImage(
    private val activity: Activity,
    private val drawable: Drawable,
    private val onPressTrace: (SketchModel) -> Unit,
    private val onPressSketch: (SketchModel) -> Unit
) {
    private lateinit var binding: DialogCreateImageBinding
    private var outputFile: File? = null

    fun onCreateDialog(): Dialog {
        binding = DialogCreateImageBinding.inflate(LayoutInflater.from(activity))
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
        binding.imvImage.setImageDrawable(drawable)
        binding.btnClose.clickWithDebounce {
            dialog.dismiss()
        }

        binding.btnShare.clickWithDebounce {
            try {
                // Convert drawable resource to bitmap
                val bitmap: Bitmap = drawable.toBitmap(400, 500)
                // Save bitmap to app cache folder
                outputFile =
                    File(activity.filesDir, "ar_draw_custom_${System.currentTimeMillis()}.png")
                val uri = FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    outputFile!!
                )
                val outputStream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()


                // Share file
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                activity.startActivity(shareIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnSketch.clickWithDebounce {
            try {
                val bitmap: Bitmap = drawable.toBitmap()
                // Save bitmap to app cache folder
                if (outputFile == null) {
                    outputFile =
                        File(activity.filesDir, "ar_draw_custom_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }

                outputFile?.let {
                    val sketchModel =
                        SketchModel(
                            id = 0,
                            remoteUrl = "",
                            freeIdRawRes = 0,
                            localUrl = it.path,
                            isFree = true,
                            isUnlocked = true
                        )
                    onPressSketch.invoke(sketchModel)
                }
                dialog.dismiss()
            } catch (e: Exception) {

            }

        }

        binding.btnTrace.clickWithDebounce {
            try {
                val bitmap: Bitmap = drawable.toBitmap()
                // Save bitmap to app cache folder
                if (outputFile == null) {
                    outputFile =
                        File(activity.filesDir, "ar_draw_custom_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
                outputFile?.let {
                    val sketchModel =
                        SketchModel(
                            id = 0,
                            remoteUrl = "",
                            freeIdRawRes = 0,
                            localUrl = it.path,
                            isFree = true,
                            isUnlocked = true
                        )
                    onPressTrace.invoke(sketchModel)
                }
                dialog.dismiss()

            } catch (e: Exception) {
            }


        }

    }
}