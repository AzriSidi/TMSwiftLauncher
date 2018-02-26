package com.plamera.tmswiftlauncher.Provider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppsVer {
    Context context;
    String versionName = "";

    public AppsVer(Context context){
        this.context = context;
    }

    public String SwiftVer(){
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo("my.com.tm.swift", 0);
            versionName = app.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public String LauncherVer() {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = app.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public String AgentVer() {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo("org.wso2.emm.agent", 0);
            versionName = app.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
