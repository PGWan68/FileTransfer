package com.demon.apport.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.demon.apport.App
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.regex.Pattern

/**
 * Created by masel on 2016/10/10.
 */
object WifiUtils {
    private const val EMPTY_IP = "0:0:0:0"

    private const val PATTERN_ETH_STR: String = "^eth\\d+$"
    private val ETH_PATTERN: Pattern = Pattern.compile(PATTERN_ETH_STR)

    /***
     * 以太网是否连接
     */
    fun isEthernetConnect(): Boolean {
        val manager =
            App.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        if (info?.type != ConnectivityManager.TYPE_ETHERNET) {
            return false
        }
        if (!info.isAvailable) {
            return false
        }
        return true
    }


    @SuppressLint("DefaultLocale")
    fun getWifiIp(): String {
        val wifiManager = App.appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        if (ipAddress == 0) {
            try {
                val enumerationNi = NetworkInterface.getNetworkInterfaces()
                while (enumerationNi.hasMoreElements()) {
                    val networkInterface = enumerationNi.nextElement()
                    val interfaceName = networkInterface.displayName
                    if (ETH_PATTERN.matcher(interfaceName)
                            .matches() || interfaceName == "wlan0"
                    ) {
                        val enumIpAddr = networkInterface.inetAddresses
                        while (enumIpAddr.hasMoreElements()) {
                            val inetAddress = enumIpAddr.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress.getHostAddress() ?: EMPTY_IP
                            }
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
        } else {
            return String.format(
                "%d.%d.%d.%d",
                (ipAddress and 0xff),
                (ipAddress shr 8 and 0xff),
                (ipAddress shr 16 and 0xff),
                (ipAddress shr 24 and 0xff)
            )
        }
        return EMPTY_IP
    }

    fun getIp(): String {
        val manager =
            App.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = manager.activeNetworkInfo
        if (activeNetInfo != null) {
            return if (activeNetInfo.type == ConnectivityManager.TYPE_WIFI) {
                getWifiIp()
            } else {
                getEthernetIp()
            }
        }
        return EMPTY_IP
    }

    /**
     * 获取以太网ip地址
     */
    private fun getEthernetIp(): String {
        var ip = ""
        try {
            val etherInterface = NetworkInterface.getNetworkInterfaces()
            while (etherInterface.hasMoreElements()) {//判断是否有数据
                val element = etherInterface.nextElement()
                if (!element.isUp) {
                    continue
                }
                if (element.displayName != "eth0") {
                    continue
                }
                val addresses = element.inetAddresses
                while (addresses.hasMoreElements()) {
                    val subElement = addresses.nextElement()
                    if (subElement.isSiteLocalAddress) {
                        ip = subElement.hostAddress
                        LogUtils.wtf(Tag, "getEthernetIp hostAddress=$ip")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.wtf(Tag, "getEthernetIp error: ", e)
        }
        LogUtils.wtf(Tag, "getEthernetIp: $ip")
        return ip.ifEmpty {
            EMPTY_IP
        }
    }

//    @SuppressLint("WifiManagerLeak")
//    private fun getWifiIp(): String {
//        val wifimanager = App.appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo = wifimanager.connectionInfo
//        return if (wifiInfo != null) {
//            intToIp(wifiInfo.ipAddress)
//        } else {
//            EMPTY_IP
//        }
//    }
//
//    private fun intToIp(i: Int): String {
//        return (i and 0xFF).toString() + "." + (i shr 8 and 0xFF) + "." + (i shr 16 and 0xFF) + "." + (i shr 24 and 0xFF)
//    }

    /**
     * 有可用的网络
     */
    fun getNetState(): Boolean {
        var flag = false
        val manager =
            App.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = manager.allNetworkInfo
        for (info in netInfo) {
            LogUtils.wtf(Tag, "getNetState: ${info.typeName}=${info.isConnected}")
            if (info.isConnected) {
                flag = true
            }
        }
        return flag
    }
}