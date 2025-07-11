package com.demon.apport.ui

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.wifi.WifiManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.demon.apport.R
import com.demon.apport.base.BaseActivity
import com.demon.apport.data.Constants
import com.demon.apport.data.InfoModel
import com.demon.apport.databinding.ActivityMainBinding
import com.demon.apport.receiver.WifiReceiver
import com.demon.apport.service.WebHelper
import com.demon.apport.service.WebService
import com.demon.apport.ui.adapter.FilesAdapter
import com.demon.apport.ui.dialog.WifiStateDialog
import com.demon.apport.util.FileUtils
import com.demon.apport.util.LogUtils
import com.demon.apport.util.Tag
import com.jeremyliao.liveeventbus.LiveEventBus

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mFiles: MutableList<InfoModel> = mutableListOf()
    private var dialog: WifiStateDialog? = null

    private val adapter by lazy {
        FilesAdapter(mFiles)
    }

    private val receiver by lazy {
        WifiReceiver()
    }

    private val filter by lazy {
        IntentFilter().apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
    }

    override fun initData() {
        initToolbar()
        initReceiver()
        initEventBus()
        initRecyclerView()
    }

    private fun initReceiver() {
        registerReceiver(receiver, filter)
    }

    private fun initToolbar() {
        setTitle(getString(R.string.app_title))

        val ivWifi = findViewById<ImageView>(R.id.ivWifi)
        ivWifi.visibility = View.VISIBLE
        ivWifi.setOnClickListener {
            LogUtils.wtf(Tag, "Server.isRunning=${WebHelper.instance.isConnected()}")

            dialog = WifiStateDialog()
            dialog?.showAllowingState(supportFragmentManager)
        }

        val ivSetting = findViewById<ImageView>(R.id.ivSetting)
        ivSetting.visibility = View.VISIBLE
        ivSetting.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        WebService.stop(this)
    }

    private fun initEventBus() {
        LiveEventBus.get<Int>(Constants.LOAD_BOOK_LIST).observe(this) {
            updateMainUI()
        }

        LiveEventBus.get<Boolean>(Constants.WIFI_CONNECT_CHANGE_EVENT).observe(this) {
            if (it) {
                WebService.start(this)
            } else {
                WebService.stop(this)
            }
        }
    }

    private fun updateMainUI() {
        val listArr: MutableList<InfoModel> = FileUtils.getAllFiles(this@MainActivity)
        runOnUiThread {
            dialog?.hide()
            binding.refreshLayout.isRefreshing = false
            mFiles.clear()
            mFiles.addAll(listArr)
            adapter.notifyDataSetChanged()
        }
    }


    private fun initRecyclerView() {
        binding.run {
            list.setHasFixedSize(true)
            list.layoutManager = LinearLayoutManager(this@MainActivity)
            list.adapter = adapter
            refreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light
            )
            refreshLayout.setOnRefreshListener {
                updateMainUI()
            }
            updateMainUI()
        }
    }


}