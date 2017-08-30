package com.example.weixin50;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by ldh on 2017/8/29.
 */

public class WifiUtil {
    private static final String TAG = "TAG";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    /**
     * 获取wifi热点的状态
     *
     * @param mContext
     * @return
     */
    public static int getWifiApState(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            Log.i(TAG, "wifi state:  " + i);
            return i;
        } catch (Exception e) {
            Log.e(TAG, "Cannot get WiFi AP state" + e);
            return WIFI_AP_STATE_FAILED;
        }
    }

    /**
     * 判断Wi-Fi热点是否可用
     *
     * @param mContext
     * @return
     */
    public boolean isApEnabled(Context mContext) {
        int state = getWifiApState(mContext);
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
    }


    /**
     * 获取链接到当前热点的设备IP：
     *
     * @return
     */
    private static ArrayList<String> getConnectedHotIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }


    //输出链接到当前设备的IP地址
    public static String getHotIp() {

        ArrayList<String> connectedIP = getConnectedHotIP();
        StringBuilder resultList = new StringBuilder();
        for (String ip : connectedIP) {
            resultList.append(ip);
            resultList.append("\n");
        }
        Log.d(TAG, "---->>heww resultList=" + resultList);
        return resultList.toString();
    }
}
