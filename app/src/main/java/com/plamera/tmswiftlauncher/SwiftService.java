package com.plamera.tmswiftlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SwiftService {
    Context context;
    Intent intent;
    String TAG = "SwiftService";

    public SwiftService(Context context) {
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
}


