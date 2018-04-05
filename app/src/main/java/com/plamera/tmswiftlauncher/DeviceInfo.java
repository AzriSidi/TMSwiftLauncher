package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceInfo {
    TelephonyManager tel;
    String imei,imsi,carrierName,firmVer,simCard,simState;
    Context context;
    Activity activity;
    DeviceOperate device;
    TextView myScroller;
    MainActivity mainActivity;
    static String TAG = "DeviceInfo";
    Intent intent;

    public DeviceInfo(Context context){
        this.context = context;
        this.activity = (Activity) context;
        device = new DeviceOperate(context);
        mainActivity = new MainActivity();
        myScroller = activity.findViewById(R.id.textView1);
        tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static class EmmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Global.EmmStatus = intent.getStringExtra("EmmStatus");
            Log.e(TAG,Global.EmmStatus!=null?Global.EmmStatus:"Not received");
        }
    }

    @SuppressLint("MissingPermission")
    public String getImei(){
        imei = tel.getDeviceId();
        if(imei == null){
            imei = "Not Available";
        }
        return imei;
    }

    @SuppressLint("MissingPermission")
    public String getImsi(){
        imsi = tel.getSimSerialNumber();
        if (imsi == null){
            noImsiPopUp();
            imsi = "Not Available";
        }
        return imsi;
    }

    public String getCarrier(){
        carrierName = tel.getNetworkOperatorName();
        if(carrierName.equals("")){
            carrierName = "Not Available";
        }
        return carrierName;
    }

    public String getSimState(){
        int ss = tel.getSimState();
        switch (ss) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                simCard = "Unknown";
                break;
            case TelephonyManager.SIM_STATE_READY:
                simCard = "Ready";
        }

        if(simCard == "Ready"){
            simState = "";
            tel.listen(new PhoneState(activity), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }else {
            simState = "Not Available";
        }
        return simState;
    }

    public String getFirmVer(){
        firmVer = Build.DISPLAY;
        return firmVer;
    }

    public String getLocalIP() {
        Boolean useIPv4 = true; // only looks for IPv4 address
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port
                                // suffix
                                return delim < 0 ? sAddr : sAddr.substring(0,
                                        delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("getLocalIP", ex.toString());
        }
        return "Not available";
    }

    public void queryNetwork() {
        String netPre = "";
        String myStatus = "";
        String activeConn = "";

        try {
            if(activity instanceof MainActivity){
                Global.myStatus = "Network: ";
                if(Global.connected3G){
                    if(Global.EmmStatus.equals("Not Active")) {
                        AgentIntent();
                    }
                }
            }else if(activity instanceof HomeScreen){
                Global.myStatus = Global.usernameBB+" | ";
            }

            ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            Global.connected3G = mobile.isConnected();
            Global.connectedToWiFi = wifi.isConnected();

            if (Global.connected3G || Global.connectedToWiFi) {
                Global.localIP = getLocalIP();
            }

            if (connMgr.getActiveNetworkInfo() != null) {
                activeConn = connMgr.getActiveNetworkInfo().getSubtypeName();
            } else {
                activeConn = "NONE";
            }

            switch (activeConn) {
                case "LTE":
                    netPre = "4G";
                    break;
                case "EDGE":
                    netPre = "E";
                    break;
                default:
                    netPre = "3G";
                    break;
            }

            String activeConnPlus = activeConn;
            if (Global.connectedToWiFi) {
                activeConnPlus = "WIFI ";
                Global.netType = "WIFI/" + device.getWifiSsid();
            }else if (Global.connected3G) {
                // Global.URLSwift = "http://10.41.102.70/";
                activeConnPlus += "";
                Global.netType = netPre+"/"+activeConnPlus;
            }else if ((!Global.connectedToWiFi) && (!Global.connected3G)) {
                activeConnPlus = "None";
                Global.ServerStatus = "Not Connected";
                Global.netType = activeConnPlus;
            }

            Global.myStatus += activeConnPlus;
            myStatus = Global.myStatus + " | Server: "
                    + Global.ServerStatus;

            if (Global.ServerStatus.contains("Not Connected")) {
                myScroller.setBackgroundColor(Color.RED);
            } else if (Global.ServerStatus.contains("Unknown")) {
                myScroller.setBackgroundColor(Color.RED);
            } else {
                if (Global.myStatus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
            myScroller.setText(myStatus);
            Log.e(TAG,"myStatus: "+myStatus);
        } catch (Exception e) {
            Log.e(TAG, "Exception: "+e.toString());

        }
    }

    public void AgentIntent(){
        intent = context.getPackageManager().getLaunchIntentForPackage("org.wso2.emm.agent");
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public void noImsiPopUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(
               context);
        builder.setMessage(
                "No IMSI detected. Please check your sim card")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                dialog.dismiss();
                                //finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
