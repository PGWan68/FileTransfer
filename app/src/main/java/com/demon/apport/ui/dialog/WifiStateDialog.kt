package com.demon.apport.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.demon.apport.R
import com.demon.apport.data.Constants
import com.demon.apport.databinding.FragmentWifiStateBinding
import com.demon.apport.util.WifiUtils
import com.demon.apport.util.dp2px
import com.jeremyliao.liveeventbus.LiveEventBus
import androidx.core.graphics.drawable.toDrawable
import com.king.zxing.util.CodeUtils

/**
 * @author DeMonnnnnn
 * @date 2022/6/20
 * @email liu_demon@qq.com
 * @desc
 */
class WifiStateDialog : DialogFragment() {

    private var _binding: FragmentWifiStateBinding? = null
    protected val binding: FragmentWifiStateBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.run {
            isCancelable = true
            setCanceledOnTouchOutside(true)
            setCancelable(true)
        }
        _binding = FragmentWifiStateBinding.inflate(layoutInflater, container, false)
        return _binding?.root
    }

    private fun padding(num: Int): Int {
        return num.dp2px
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.run {
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(Color.DKGRAY.toDrawable())
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
            )
//            decorView.setPadding(padding(20), padding(20), padding(10), padding(10))
        }


        binding.mBtnWifiSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        if (WifiUtils.getNetState()) {
            onWifiConnected()
        } else {
            onWifiDisconnected()
        }

        LiveEventBus.get<Boolean>(Constants.WIFI_CONNECT_CHANGE_EVENT).observe(this) {
            changeState(it)
        }

    }

    private fun changeState(state: Boolean) {
        if (state) onWifiConnected()
        else onWifiDisconnected()
    }

    private fun onWifiDisconnected() {
        binding.run {
            mTxtTitle.setText(R.string.wlan_disabled)
            mTxtTitle.setTextColor(requireContext().resources.getColor(android.R.color.black))
            mTxtSubTitle.visibility = View.VISIBLE
            mImgLanState.setImageResource(R.drawable.shared_wifi_shut_down)
            mTxtStateHint.setText(R.string.fail_to_start_http_service)
            mTxtAddress.visibility = View.GONE
            mBtnWifiSettings.visibility = View.VISIBLE
        }
    }

    private fun onWifiConnecting() {
        binding.run {
            mTxtTitle.setText(R.string.wlan_enabled)
            mTxtTitle.setTextColor(requireContext().resources.getColor(R.color.colorWifiConnected))
            mTxtSubTitle.visibility = View.GONE
            mImgLanState.setImageResource(R.drawable.shared_wifi_enable)
            mTxtStateHint.setText(R.string.retrofit_wlan_address)
            mTxtAddress.visibility = View.GONE
            mBtnWifiSettings.visibility = View.GONE
        }
    }

    private fun onWifiConnected() {
        binding.run {
            mTxtTitle.setText(R.string.wlan_enabled)
            mTxtTitle.setTextColor(requireContext().resources.getColor(R.color.colorWifiConnected))
            mTxtSubTitle.visibility = View.GONE
//            mImgLanState.setImageResource(R.drawable.shared_wifi_enable)
            mTxtStateHint.setText(R.string.pls_input_the_following_address_in_pc_browser)
            mTxtAddress.visibility = View.VISIBLE
            val ipAddr = WifiUtils.getWifiIp()
            val strText = String.format(
                requireContext().getString(R.string.http_address), ipAddr, Constants.HTTP_PORT
            )
            mTxtAddress.text = strText
            mBtnWifiSettings.visibility = View.GONE
            val qrCode = CodeUtils.createQRCode(strText, 300)
            mImgLanState.setImageBitmap(qrCode)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 关闭弹窗的时候调用dismissAllowingStateLoss
     */
    open fun showAllowingState(manager: FragmentManager) {
        showAllowingState(manager, tag)
    }

    /**
     * 关闭弹窗的时候调用dismissAllowingStateLoss
     */
    open fun showAllowingState(manager: FragmentManager, tagStr: String? = null) {
        manager.beginTransaction().add(this, tagStr ?: tag).commitAllowingStateLoss()
    }


    open fun hide() {
        if (dialog?.isShowing == true) {
            dismissAllowingStateLoss()
        }

    }

}