package com.example.arsketch.ui.sketch

import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arsketch.MainApplication
import com.example.arsketch.R
import com.example.arsketch.common.Constant
import com.example.arsketch.common.DownloadManagerApp
import com.example.arsketch.common.ads.AdsCore
import com.example.arsketch.common.ads.InterAdsEnum
import com.example.arsketch.common.ads.NativeTypeEnum
import com.example.arsketch.common.base_component.BaseActivity
import com.example.arsketch.common.clickWithDebounce
import com.example.arsketch.common.config
import com.example.arsketch.common.getFileNameFromURL
import com.example.arsketch.common.isInternetAvailable
import com.example.arsketch.data.model.DataCategory
import com.example.arsketch.data.model.SketchModel
import com.example.arsketch.databinding.ActivityListSketchDataBinding
import com.example.arsketch.ui.challenge.ChallengeActivity
import com.example.arsketch.ui.dialog.DialogLoadingProgress
import com.example.arsketch.ui.trace.TraceActivity
import com.example.arsketch.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListSketchDataActivity : BaseActivity<ActivityListSketchDataBinding>(
    ActivityListSketchDataBinding::inflate, { true }) {
    private val viewModel by viewModels<AppViewModel>()
    private var adapterSketchList: AdapterSketchList? = null

    override fun initView() {
        loadAds()
        changeStatusBarColor(R.color.white)
        initRecyclerView()
        val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
        dataCategory?.let {
            binding.tvTitle.text = it.name

            val mListModel = mutableListOf<SketchModel>()
            listFree[it.id]?.forEachIndexed { index, drawableId ->
                mListModel.add(
                    SketchModel(
                        id = index,
                        localUrl = "",
                        remoteUrl = "",
                        freeIdRawRes = drawableId,
                        isUnlocked = true,
                        isFree = true
                    )
                )
            }
            val newList = retrieveDownloadedItem(mListModel)
            adapterSketchList?.setData(newList)

        }

        if (isInternetAvailable(this)) {
            if (MainApplication.app.dataSource.value?.isNotEmpty() == true) {
                val dataCategory =
                    intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
                dataCategory?.let {
                    binding.tvTitle.text = it.name
                    observeDataMain(it.id)
                }
            } else {
                viewModel.getListDataSource()
                val dataCategory =
                    intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
                dataCategory?.let {
                    binding.tvTitle.text = it.name
                    observeData(it.id)
                }
            }

        } else {
            val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
            dataCategory?.let {
                binding.tvTitle.text = it.name

                val mListModel = mutableListOf<SketchModel>()
                listFree[it.id]?.forEachIndexed { index, drawableId ->
                    mListModel.add(
                        SketchModel(
                            id = index,
                            localUrl = "",
                            remoteUrl = "",
                            freeIdRawRes = drawableId,
                            isUnlocked = true,
                            isFree = true
                        )
                    )
                }
                val newList = retrieveDownloadedItem(mListModel)
                adapterSketchList?.setData(newList)

            }
        }



        binding.btnBack.clickWithDebounce {
            showInterAds(action = {
                finish()
            }, actionFailed = {
                finish()
            }, InterAdsEnum.BACK)
        }

        changeBackPressCallBack {
            showInterAds(action = {
                finish()
            }, actionFailed = {
                finish()
            }, InterAdsEnum.BACK)
        }
    }


    private fun initRecyclerView() {
        adapterSketchList = AdapterSketchList(onClickItem = {
            showInterAds(action = {
                if (MainApplication.app.isChallenge) {
                    val mIntent = Intent(this, ChallengeActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                    startActivity(mIntent)
                } else {
                    if (MainApplication.app.isSketch) {
                        val mIntent = Intent(this, SketchActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)
                    } else {
                        val mIntent = Intent(this, TraceActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)
                    }
                }
            }, actionFailed = {
                if (MainApplication.app.isChallenge) {
                    val mIntent = Intent(this, ChallengeActivity::class.java)
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                    startActivity(mIntent)
                } else {
                    if (MainApplication.app.isSketch) {
                        val mIntent = Intent(this, SketchActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)
                    } else {
                        val mIntent = Intent(this, TraceActivity::class.java)
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, it)
                        startActivity(mIntent)
                    }
                }
            }, InterAdsEnum.DRAW)



        }, onClickVipItem = { model, position ->
//            val dialogWatchAds = DialogWatchAds(this, onClickWatch = {
//                showRewardAds(action = {
            if (!isInternetAvailable(this)) {
                Toast.makeText(
                    this, getString(R.string.internet_not_available), Toast.LENGTH_SHORT
                ).show()
            } else {
                showInterAds(action = {
                    showProgress(model,position)
                }, actionFailed = {
                    showProgress(model,position)
                }, InterAdsEnum.DRAW)

            }
//                }, actionFailed = {}, RewardTypeEnum.DOWNLOAD)

//            })
//            dialogWatchAds.show()
        })
        val gridLayoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rcvSketch.layoutManager = gridLayoutManager
        binding.rcvSketch.adapter = adapterSketchList
    }

    private fun showProgress(model: SketchModel,position:Int) {
        val dialogLoadingProgress = DialogLoadingProgress(this, onEnd = {
            val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
            val downloadedItem = config.downloadedMap.toMutableMap()
            if (downloadedItem[dataCategory!!.id] == null) {
                downloadedItem[dataCategory.id] = mutableListOf()
            }
            val downloadedCategory = downloadedItem[dataCategory.id]?.toMutableList()
            downloadedCategory?.add(
                Pair(
                    model.id, filesDir.path + "/image/" + model.remoteUrl.getFileNameFromURL()
                )
            )
            downloadedItem[dataCategory.id] = downloadedCategory!!
            config.downloadedMap = downloadedItem
            lifecycleScope.launch(Dispatchers.Main) {
                adapterSketchList?.notifyItem(position)
                if (MainApplication.app.isChallenge) {
                    val mIntent = Intent(
                        this@ListSketchDataActivity, ChallengeActivity::class.java
                    )
                    mIntent.putExtra(Constant.KEY_SKETCH_MODEL, model)
                    startActivity(mIntent)
                } else {
                    if (MainApplication.app.isSketch) {
                        val mIntent = Intent(
                            this@ListSketchDataActivity, SketchActivity::class.java
                        )
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, model)
                        startActivity(mIntent)
                    } else {
                        val mIntent = Intent(
                            this@ListSketchDataActivity, TraceActivity::class.java
                        )
                        mIntent.putExtra(Constant.KEY_SKETCH_MODEL, model)
                        startActivity(mIntent)
                    }
                }

            }
        })
        dialogLoadingProgress.show()
        DownloadManagerApp.getInstance(this).makeRequestDownload(
            model.remoteUrl,
            filesDir.path + "/image",
            model.remoteUrl.getFileNameFromURL(),
            onDownloadCompleted = {
                dialogLoadingProgress.complete()
            },
            onDownloadFailed = {
                Toast.makeText(
                    this, getString(R.string.error_orccured), Toast.LENGTH_SHORT
                ).show()
                dialogLoadingProgress?.dismiss()
            },
            onProgressListener = {
                dialogLoadingProgress.setValue(it)
            })
    }
    private fun observeData(index: Int) {
        val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
        dataCategory?.let { dataCategory ->
            val mListModel = mutableListOf<SketchModel>()
            listFree[dataCategory.id]?.forEachIndexed { index, drawableId ->
                mListModel.add(
                    SketchModel(
                        id = index,
                        localUrl = "",
                        remoteUrl = "",
                        freeIdRawRes = drawableId,
                        isUnlocked = false,
                        isFree = true
                    )
                )
            }
            val newList = retrieveDownloadedItem(mListModel)
            viewModel.dataSource.observe(this) {
                it[index.toString()]?.forEachIndexed { index, item ->
                    if (index !in newList.map { it.id }) {
                        newList.add(
                            SketchModel(
                                id = index,
                                localUrl = filesDir.path + "/image/" + item.getFileNameFromURL(),
                                remoteUrl = item,
                                freeIdRawRes = 0,
                                isUnlocked = false,
                                isFree = false
                            )
                        )
                        adapterSketchList?.setData(newList)
                    }
                }

            }
        }
    }

    private fun observeDataMain(index: Int) {
        val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
        dataCategory?.let { dataCategory ->
            val mListModel = mutableListOf<SketchModel>()
            listFree[dataCategory.id]?.forEachIndexed { index, drawableId ->
                mListModel.add(
                    SketchModel(
                        id = index,
                        localUrl = "",
                        remoteUrl = "",
                        freeIdRawRes = drawableId,
                        isUnlocked = false,
                        isFree = true
                    )
                )
            }
            val newList = retrieveDownloadedItem(mListModel)
            MainApplication.app.dataSource.observe(this) {
                it[index.toString()]?.forEachIndexed { index, item ->
                    if (index !in newList.map { it.id }) {
                        newList.add(
                            SketchModel(
                                id = index,
                                localUrl = filesDir.path + "/image/" + item.getFileNameFromURL(),
                                remoteUrl = item,
                                freeIdRawRes = 0,
                                isUnlocked = false,
                                isFree = false
                            )
                        )
                        adapterSketchList?.setData(newList)
                    }
                }

            }
        }
    }

    private fun retrieveDownloadedItem(rawList: MutableList<SketchModel>): MutableList<SketchModel> {
        val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
        val newList = rawList.toMutableList()
        dataCategory?.let {
            val downloadedItem = config.downloadedMap.toMutableMap()
            val downloadedCategory = downloadedItem[it.id]
            downloadedCategory?.let {
                it.forEach { pair ->
                    val newSketch = SketchModel(
                        id = pair.first,
                        localUrl = pair.second,
                        remoteUrl = "",
                        freeIdRawRes = 0,
                        isUnlocked = true,
                        isFree = false
                    )
                    newList.add(newSketch)
                }
            }
        }
        return newList
    }

    private fun solveList(mListLMutableList: MutableList<SketchModel>): MutableList<SketchModel> {
        val dataCategory = intent.getParcelableExtra<DataCategory>(Constant.KEY_SKETCH_CATEGORY)
        val newList = mListLMutableList.toMutableList()
        dataCategory?.let {
            val downloadedItem = config.downloadedMap.toMutableMap()
            val downloadedCategory = downloadedItem[it.id]
            downloadedCategory?.let {
                it.forEach { pair ->
                    newList.find { it.id == pair.first }.let {
                        it?.localUrl = pair.second
                        it?.isUnlocked = true
                    }
                }
            }
        }
        return newList
    }

    private val listFree = mutableMapOf(
        0 to mutableListOf(
            R.drawable.animal01,
            R.drawable.animal02,
            R.drawable.animal03,
            R.drawable.animal04,
            R.drawable.animal05,
            R.drawable.animal06,
            R.drawable.animal07,
            R.drawable.animal08,
        ),
        1 to mutableListOf(
            R.drawable.chibi01,
            R.drawable.chibi02,
            R.drawable.chibi03,
            R.drawable.chibi04,
            R.drawable.chibi05,
            R.drawable.chibi06,
            R.drawable.chibi07,
            R.drawable.chibi08,
        ),
        2 to mutableListOf(
            R.drawable.flower01,
            R.drawable.flower02,
            R.drawable.flower03,
            R.drawable.flower04,
            R.drawable.flower05,
            R.drawable.flower06,
            R.drawable.flower07,
            R.drawable.flower08,
        ),
        3 to mutableListOf(
            R.drawable.forkid01,
            R.drawable.forkid02,
            R.drawable.forkid03,
            R.drawable.forkid04,
            R.drawable.forkid05,
            R.drawable.forkid06,
            R.drawable.forkid07,
            R.drawable.forkid08,
        ),
        4 to mutableListOf(
            R.drawable.learntodraw01,
            R.drawable.learntodraw02,
            R.drawable.learntodraw03,
            R.drawable.learntodraw04,
            R.drawable.learntodraw05,
            R.drawable.learntodraw06,
            R.drawable.learntodraw07,
            R.drawable.learntodraw08,
        ),
        5 to mutableListOf(
            R.drawable.manga01,
            R.drawable.manga02,
            R.drawable.manga03,
            R.drawable.manga04,
            R.drawable.manga05,
            R.drawable.manga06,
            R.drawable.manga07,
            R.drawable.manga08,
        ),
        6 to mutableListOf(
            R.drawable.nature01,
            R.drawable.nature02,
            R.drawable.nature03,
            R.drawable.nature04,
            R.drawable.nature05,
            R.drawable.nature06,
            R.drawable.nature07,
            R.drawable.nature08,
        ),
        7 to mutableListOf(
            R.drawable.people01,
            R.drawable.people02,
            R.drawable.people03,
            R.drawable.people04,
            R.drawable.people05,
            R.drawable.people06,
            R.drawable.people07,
            R.drawable.people08,
        ),
        8 to mutableListOf(
            R.drawable.christmas01,
            R.drawable.christmas02,
            R.drawable.christmas03,
            R.drawable.christmas04,
            R.drawable.christmas05,
            R.drawable.christmas06,
            R.drawable.christmas07,
            R.drawable.christmas08,
        ),
        9 to mutableListOf(
            R.drawable.cute01,
            R.drawable.cute02,
            R.drawable.cute03,
            R.drawable.cute04,
            R.drawable.cute05,
            R.drawable.cute06,
            R.drawable.cute07,
            R.drawable.cute08,
        ),
        10 to mutableListOf(
            R.drawable.face01,
            R.drawable.face02,
            R.drawable.face03,
            R.drawable.face04,
            R.drawable.face05,
            R.drawable.face06,
            R.drawable.face07,
            R.drawable.face08,
        ),
        11 to mutableListOf(
            R.drawable.food01,
            R.drawable.food02,
            R.drawable.food03,
            R.drawable.food04,
            R.drawable.food05,
            R.drawable.food06,
            R.drawable.food07,
            R.drawable.food08,
        ),
        12 to mutableListOf(
            R.drawable.tattoo01,
            R.drawable.tattoo02,
            R.drawable.tattoo03,
            R.drawable.tattoo04,
            R.drawable.tattoo05,
            R.drawable.tattoo06,
            R.drawable.tattoo07,
            R.drawable.tattoo08,
        ),
        13 to mutableListOf(
            R.drawable.vegetable01,
            R.drawable.vegetable02,
            R.drawable.vegetable03,
            R.drawable.vegetable04,
            R.drawable.vegetable05,
            R.drawable.vegetable06,
            R.drawable.vegetable07,
            R.drawable.vegetable08,
        ),
        14 to mutableListOf(
            R.drawable.vehicle01,
            R.drawable.vehicle02,
            R.drawable.vehicle03,
            R.drawable.vehicle04,
            R.drawable.vehicle05,
            R.drawable.vehicle06,
            R.drawable.vehicle07,
            R.drawable.vehicle08,
        ),
        15 to mutableListOf(
            R.drawable.halloween1,
            R.drawable.halloween2,
            R.drawable.halloween3,
            R.drawable.halloween4,
        ),
        16 to mutableListOf(
            R.drawable.tet_1,
            R.drawable.tet_2,
            R.drawable.tet_3,
            R.drawable.tet_4,
        ),
        17 to mutableListOf(
            R.drawable.valentine_1,
            R.drawable.valentine_2,
            R.drawable.valentine_3,
            R.drawable.valentine_4,
        ),

        )

    private fun loadAds() {
        AdsCore.showNativeAds(this, binding.nativeAdmob, {

        }, {

        }, {}, NativeTypeEnum.CATEGORY_2)
    }
}