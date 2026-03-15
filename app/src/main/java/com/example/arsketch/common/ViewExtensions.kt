package com.example.arsketch.common

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.arsketch.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException

import java.net.URL


fun View.clickWithDebounce(debounceTime: Long = 400L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}
fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
)

fun ImageView.loadImageUrl(
    url: String?,
    onSuccess: ((drawable: Drawable) -> Unit)? = null,
    onFail: ((fail: Boolean) -> Unit)? = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val chefBitmap: Drawable = withContext(Dispatchers.IO) {
                Glide.with(context).asDrawable().load(url).skipMemoryCache(true)
                    .listener(object : RequestListener<Drawable> {


                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            onFail?.invoke(false)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return true
                        }
                    })
                    .error(R.drawable.ic_error_image).submit().get()
            }
            withContext(Dispatchers.Main) {
                onSuccess?.invoke(chefBitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

fun View.clickWithDebounce(
    debounceTime: Long = 400L, action: () -> Unit, actionFailed: () -> Unit
) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) actionFailed()
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Activity.blockScreenShot() {
    window.addFlags(
        WindowManager.LayoutParams.FLAG_SECURE
    )
}

fun Activity.clearFlag() {
    window.clearFlags(
        WindowManager.LayoutParams.FLAG_SECURE
    )

}

//fun Activity.checkScreenProhibit() {
//    if (config.prohibitScreenShot) {
//        blockScreenShot()
//    } else {
//        clearFlag()
//    }
//}

fun String.getFileNameFromURL(): String {
    try {
        val resource = URL(this)
        val host = resource.host
        if (host.length > 0 && this.endsWith(host)) {
            // handle ...example.com
            return ""
        }
    } catch (e: MalformedURLException) {
        return ""
    }
    val startIndex = this.lastIndexOf('/') + 1
    val length = this.length

    // find end index for ?
    var lastQMPos = this.lastIndexOf('?')
    if (lastQMPos == -1) {
        lastQMPos = length
    }

    // find end index for #
    var lastHashPos = this.lastIndexOf('#')
    if (lastHashPos == -1) {
        lastHashPos = length
    }

    // calculate the end index
    val endIndex = Math.min(lastQMPos, lastHashPos)
    return this.substring(startIndex, endIndex)
}



