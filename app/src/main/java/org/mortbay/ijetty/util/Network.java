package org.mortbay.ijetty.util;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;


/**
 *
 */
public class Network
{
    public static String getLocalIp(Context context)
    {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);//获取WifiManager

        //检查wifi是否开启
        if (!wifiManager.isWifiEnabled())
        {
            return null;
        }

        WifiInfo wifiinfo = wifiManager.getConnectionInfo();

        String ip = intToIp(wifiinfo.getIpAddress());

        return ip;
    }

    private static String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
            + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }


    public final static String NETWORK_CMNET = "CMNET";
    public final static String NETWORK_CMWAP = "CMWAP";
    public final static String NETWORK_WIFI = "WIFI";
    public final static String TAG = "NetworkUtils";
    private static NetworkInfo networkInfo = null;
    private Context mContext = null;

    private Network() {
    }

    public static Network getInstance() {
        return SingletonHolder.instance;
    }

    public Network init(Context context) {
        this.mContext = context;
        return this;
    }

    /**
     * 判断网络是否可用
     *
     * @return 是/否
     */
    public boolean isAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (null == manager) {
            return false;
        }
        networkInfo = manager.getActiveNetworkInfo();
        if (null == networkInfo || !networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    /**
     * 判断网络是否已连接
     *
     * @return 是/否
     */
    public boolean isConnected() {
        if (!isAvailable()) {
            return false;
        }
        if (!networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    /**
     * 检查当前环境网络是否可用，不可用跳转至开启网络界面,不设置网络强制关闭当前Activity
     */
    public void validateNetWork() {

        if (!isConnected()) {
            Builder dialogBuilder = new Builder(mContext);
            dialogBuilder.setTitle("网络设置");
            dialogBuilder.setMessage("网络不可用，是否现在设置网络？");
            dialogBuilder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) mContext)
                                    .startActivityForResult(new Intent(
                                            Settings.ACTION_SETTINGS), which);
                        }
                    });
            dialogBuilder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            dialogBuilder.create();
            dialogBuilder.show();
        }
    }

    /**
     * 获取网络连接信息</br> 无网络：</br> WIFI网络：WIFI</br> WAP网络：CMWAP</br>
     * NET网络：CMNET</br>
     *
     * @return
     */
    public String getNetworkType() {
        if (isConnected()) {
            int type = networkInfo.getType();
            if (ConnectivityManager.TYPE_MOBILE == type) {
                Log.i(TAG,
                        "networkInfo.getExtraInfo()-->"
                                + networkInfo.getExtraInfo());
                if (NETWORK_CMWAP.equals(networkInfo.getExtraInfo()
                        .toLowerCase())) {
                    return NETWORK_CMWAP;
                } else {
                    return NETWORK_CMNET;
                }
            } else if (ConnectivityManager.TYPE_WIFI == type) {
                return NETWORK_WIFI;
            }
        }

        return "";
    }

    /**
     * 网络是否连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * WIFI是否连接
     * @param context
     * @return
     */
    public static boolean isWIFIConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 移动网络是否打开
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取网络连接类型
     * ConnectivityManager.TYPE_MOBILE
     * ConnectivityManager.TYPE_WIFI
     * @param context
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    private static class SingletonHolder {

        private static Network instance = new Network();
    }
}
