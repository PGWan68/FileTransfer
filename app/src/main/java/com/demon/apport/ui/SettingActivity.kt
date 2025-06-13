package com.demon.apport.ui

import android.content.Intent
import android.os.Environment
import android.widget.Toast
import com.demon.apport.App
import com.demon.apport.base.BaseActivity
import com.demon.apport.data.Constants
import com.demon.apport.databinding.ActivitySettingBinding
import com.demon.apport.util.FileUtils
import com.demon.apport.util.get
import com.demon.apport.util.mmkv
import com.demon.qfsolution.utils.getExternalOrFilesDirPath

class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    override fun initData() {
        setTitle("设置")

        binding.run {

            tvPath.setOnClickListener {
                val intent = Intent(this@SettingActivity, FilesPathActivity::class.java)
                startActivity(intent)
            }
            tvDelete.setOnClickListener {
                val def: String =
                    App.appContext.getExternalOrFilesDirPath(Environment.DIRECTORY_DCIM)
                FileUtils.deleteAll(mmkv.get(Constants.MMKV_STORAGE_PATH, def))
                Toast.makeText(this@SettingActivity, "已全部删除", Toast.LENGTH_SHORT).show()
            }
            tvLog.setOnClickListener {
                val intent = Intent(this@SettingActivity, LogListActivity::class.java)
                startActivity(intent)
            }
        }
    }
}