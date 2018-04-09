package com.plamera.tmswiftlauncher.Device;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.plamera.tmswiftlauncher.Global;
import com.plamera.tmswiftlauncher.MainActivity;
import com.plamera.tmswiftlauncher.TrackLogService;

public class DeviceService {
    Context context;
    Intent intent;
    String TAG;
    String swfitPackage = "my.com.tm.swift";
    String swiftClass = "my.com.tmrnd.swift.LocationUpdateService";
    String trackClass = "com.plamera.tmswiftlauncher.TrackLogService";

    public DeviceService(Context context) {
        this.context = context;
        TAG = context.getClass().getSimpleName();
    }

    public void startSwift(){
        try {
            intent = new Intent();
            intent.setComponent(new ComponentName(swfitPackage,swiftClass));
            intent.putExtra("staffID", Global.usernameBB);
            intent.putExtra("password", Global.passwordBB);
            intent.putExtra("imei", Global.IMEIPhone);
            intent.putExtra("imsi", Global.IMSIsimCardPhone);
            intent.putExtra("firmVer", Global.frmVersion);
            intent.putExtra("serverStatus", Global.loginServer);
            intent.putExtra("loginType", Global.UserType);
            intent.putExtra("token", Global.getToken);
            context.startService(intent);
        }catch (Exception ex) {
            Log.e(TAG,"BroadcastExeception: "+ex.toString());
        }
    }

    public void stopSwift(){
        intent = new Intent();
        intent.setComponent(new ComponentName(swfitPackage,swiftClass));
        context.stopService(intent);
    }

    public void startTrackLog(){
        try {
            intent = new Intent(context,TrackLogService.class);
            context.startService(intent);
        }catch (Exception ex){
            Log.e(TAG,"Exeception: "+ex.toString());
        }
    }

    public boolean SwiftServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (swiftClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean TrackServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (trackClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void intentAgent(){
        try {
            intent = new Intent();
            intent.setComponent(new ComponentName("org.wso2.emm.agent", "org.wso2.emm.agent.BroadcastService"));
            context.startService(intent);
        }catch (Exception ex){
            Log.d(TAG,"broadcastExeception: "+ex.toString());
        }
    }

    public void logOut(){
        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Global.status = "Offline";
    }
}


