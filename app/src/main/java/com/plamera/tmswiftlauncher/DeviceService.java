package com.plamera.tmswiftlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceService {
    Context context;
    Intent intent;
    String TAG = "SwiftService";
    DatabaseHandler db;

    public DeviceService(Context context) {
        db = new DatabaseHandler(context);
        this.context = context;
    }

    public void startSwift(){
        try {
            intent = new Intent();
            intent.setComponent(new ComponentName("my.com.tm.swift", "my.com.tmrnd.swift.LocationUpdateService"));
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
            Log.d(TAG,"BroadcastExeception: "+ex.toString());
        }
    }

    public void stopSwift(){
        intent = new Intent();
        intent.setComponent(new ComponentName("my.com.tm.swift",
                "my.com.tmrnd.swift.LocationUpdateService"));
        context.stopService(intent);
    }

    public void startTrackLog(){
        try {
            intent = new Intent(context, TrackLogService.class);
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


