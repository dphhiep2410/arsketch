package com.example.arsketch.common

import android.content.Context
import android.util.Log
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class DownloadManagerApp(val context: Context) {
    private var config: PRDownloaderConfig? = null
    private var currentIdDownload = -1
    private var scope: CoroutineScope? = null
    private var job: Job? = null

    companion object {
        lateinit var INSTANCE: DownloadManagerApp

        @JvmStatic
        fun getInstance(context: Context): DownloadManagerApp {
            if (!Companion::INSTANCE.isInitialized) {
                INSTANCE = DownloadManagerApp(context)
            }
            return INSTANCE
        }

    }

    init {
        initDownloadManager()
    }
    fun trustAllCertificates() {
        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(
                        certs: Array<X509Certificate>,
                        authType: String
                    ) {
                    }
                    override fun checkServerTrusted(
                        certs: Array<X509Certificate>,
                        authType: String
                    ) {
                        // Do nothing to bypass the server SSL verification
                    }
                }
            )
            val sc: SSLContext = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
    }
    private fun initDownloadManager() {
        trustAllCertificates()
        config = PRDownloaderConfig.newBuilder().setConnectTimeout(10000).setReadTimeout(10000)
            .setDatabaseEnabled(true).build()
        PRDownloader.initialize(context, config)
        scope = CoroutineScope(Dispatchers.IO)
    }

    fun makeRequestDownload(
        url: String,
        filePath: String,
        fileName: String,
        onProgressListener: (value: Float) -> Unit,
        onDownloadCompleted: () -> Unit,
        onDownloadFailed: (t:Throwable?) -> Unit
    ) {
        stopDownload()
        job = scope?.launch {
            currentIdDownload = PRDownloader.download(url, filePath, fileName).build()
                .setOnStartOrResumeListener { }.setOnPauseListener { }.setOnCancelListener { }
                .setOnProgressListener {
                    onProgressListener((it.currentBytes.toFloat() / it.totalBytes.toFloat()))
                }.start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        Log.d("TAG", "onDownloadComplete: ")
                        onDownloadCompleted.invoke()
                    }

                    override fun onError(error: com.downloader.Error?) {
                        Log.d("TAG", "Error: "+error?.serverErrorMessage)
                        Log.d("TAG", "Error: "+error?.isServerError)
                        Log.d("TAG", "Error: "+error?.connectionException)
                        Log.d("TAG", "Error: "+error?.responseCode)
                        onDownloadFailed.invoke(error?.connectionException)
                    }

                })
        }
    }

    fun stopDownload() {
        if (currentIdDownload != -1) {
            PRDownloader.cancel(currentIdDownload)
            PRDownloader.cancelAll()
        }
    }
}