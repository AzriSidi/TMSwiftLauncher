package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

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

    public DeviceInfo(Context context){
        this.context = context;
        this.activity = (Activity) context;
        tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
