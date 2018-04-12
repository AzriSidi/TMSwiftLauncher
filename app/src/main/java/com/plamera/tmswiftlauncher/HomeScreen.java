package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.plamera.tmswiftlauncher.Device.DeviceInfo;
import com.plamera.tmswiftlauncher.Device.DeviceOperate;
import com.plamera.tmswiftlauncher.Device.DeviceService;
import com.plamera.tmswiftlauncher.Encap.UserDetail;

import java.util.Date;
import java.util.List;
import java.util.Timer;

public class HomeScreen extends FragmentActivity {
    public static DatabaseHandler db;
    Boolean checkServerRunning = false;
    Boolean CheckNetworkRunning = false;
    Boolean clicked = false;
    Boolean click = false;
    @SuppressLint("StaticFieldLeak")
    public static TextView myScroller,notifyTask,notifyQueue,
            networkProvider,broadcastInfo,swiftVer,
            agentVer,serverName,appVer;
    BroadcastReceiver networkStateReceiver;
    Timer CheckNetworkTimer;
    TelephonyManager tel;
    static AlertDialog.Builder alertDialog;
    Intent intent;
    String TAG = "HomeScreen";
    public boolean logininvisible = false;
    static HomeScreen instance;
    DeviceOperate deviceOperate;
    int checkNetwork = 15000;
    IntentFilter iFilter,networkIntentFilter;
    DeviceService deviceService;
    DeviceInfo deviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.home_screen);
        myScroller = findViewById(R.id.textView1);
        networkProvider = findViewById(R.id.textView2);
        broadcastInfo = findViewById(R.id.textView4);
        swiftVer = findViewById(R.id.textView17);
        agentVer = findViewById(R.id.textView18);
        serverName = findViewById(R.id.textView19);
        appVer = findViewById(R.id.textView20);
        notifyTask = findViewById(R.id.badge_notification);
        notifyQueue = findViewById(R.id.badge_notification_1);
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        db = new DatabaseHandler(this);
        deviceOperate = new DeviceOperate(this);
        deviceService = new DeviceService(this);
        deviceInfo = new DeviceInfo(this);
        getSwiftApp();
        IntentData();
        deviceService.intentAgent();
        if(!deviceService.SwiftServiceRunning()){
            deviceService.startSwift();
        }else if(!deviceService.TrackServiceRunning()){
            deviceService.startTrackLog();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            CheckNetworkRunning = false;
            Global.CreateMainMenu = false;
            Global.TTreqSummDate = "";
            Global.FFreqSummDate = "";
            deviceInfo.queryNetwork();
            deviceState();
            CheckNetworkTimer = new Timer();
            CheckNetworkTimer.schedule(new DeviceInfo.CheckNetworkTimerMethod(),0,checkNetwork);
            displayReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // check APN
        String numeric = getAPN();
        if (numeric.contains("50219") || numeric.contains("50213")) {
            Global.currentAPN = "Celcom";
            Log.d("getAPN", "Celcom");
        } else if (numeric.contains("50217") || numeric.contains("50212")) {
            Global.currentAPN = "Maxis";
            Log.d("getAPN", "Maxis");
        } else {
            Global.currentAPN = "Unknown";
            Log.d("getAPN", "Unknown");
        }
    }

    private String getAPN() {
        String numeric = "";
        try {
            final Uri PREFERRED_APN_URI = Uri
                    .parse("content://telephony/carriers/preferapn");
            Cursor c = getContentResolver().query(PREFERRED_APN_URI, null,
                    null, null, null);
            c.moveToFirst();
            int index = c.getColumnIndex("numeric");
            numeric = c.getString(index);
            Log.d("getAPN", numeric);
        } catch (Exception e) {
            Log.d("getAPN", e.toString());
        }
        return numeric;
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ConnectivityManager connMgr = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressWarnings("unused")
            android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            logininvisible = false;
            Global.LogAsAdmin = false;
            // 2014-09-25 amir debug
            Global.lastAlertTime = new Date(1977, 4, 17);
            // moved to here by amir 2013-01-14
            // added by amir 2012-12-20
            Global.detailsStillEmpty = true;
            // added by amir 2013-01-02
            Global.AssignInprogressCount = 0;
            Global.OutstandingMA = 0;// pian tambah 11/07/2013
            Global.TTApointList = "";
            Global.FFreqSummDate = "";
            Global.FFAssignCount = 0;
            Global.allTaskCounter = 0;
            Global.countCF = 0;
            deviceInfo.initTask();

            networkStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    CheckNetworkRunning = false;
                    checkServerRunning = false;
                    Global.CanPing = true;
                    deviceInfo.queryNetwork();
                    deviceState();
                }
            };
            networkIntentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateReceiver, networkIntentFilter);
            iFilter = new IntentFilter("com.plamera.CUSTOM_INTENT");
            registerReceiver(MyReceiver, iFilter);
        } catch (Exception e) {
            Log.e(TAG, "Error onstart " + e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(MyReceiver);
        CheckNetworkTimer.cancel();
    }

    @Override
    public void onDestroy(){
        try{
            deviceOperate.unregisterReceiver(this);
        }catch(Exception e) {
            Log.e(TAG,"Exception: "+e.toString());
        }
        super.onDestroy();
    }

    public void IntentData(){
        intent = getIntent();
        Global.DisplayUsername = intent.getStringExtra("username");
        Global.usernameBB = intent.getStringExtra("dataStaff");
        Global.passwordBB = intent.getStringExtra("password");
        Global.loginServer = intent.getStringExtra("loginServer");
        Global.UserType = intent.getStringExtra("loginType");
        Global.IMEIPhone = intent.getStringExtra("imei");
        Global.IMSIsimCardPhone = intent.getStringExtra("imsi");
        Global.frmVersion = intent.getStringExtra("firm_ver");
        Global.strVersion = intent.getStringExtra("strVersion");
    }

    public void deviceState(){
        Global.signalStrength = deviceInfo.getSimState();
        networkProvider.setText(Global.carrierName+" | "+Global.localIP+" | "+Global.signalStrength);
    }

    private void getSwiftApp() {
        try {
            String loginState = "";
            List<UserDetail> userDetails = Global.mySQLiteAdapter.getAllContacts();
            for (UserDetail con : userDetails) {
                Global.ldapStatus = con.get_ldap();
            }
            if(Global.ldapStatus.contains("true")){
                loginState = "LDAP";
            }else if(Global.ldapStatus.contains("false")){
                loginState = "LOCAL";
            }
            appVer.setText("LAUNCHER - "+ Global.launcherVer + " | EMM - "+Global.agentVer
                    + " | SWIFT - "+Global.swiftVer +" | "+Global.loginServer+" | "+loginState);
        }catch (NullPointerException e) {
           Log.e(TAG,"NullPointerException: "+e);
        }
    }

    public BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        String TAG = "MyReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Global.getMessage = intent.getStringExtra("dataMessage");
                Global.getTask = intent.getStringExtra("dataTask");
                Global.getQueue = intent.getStringExtra("dataQueue");
                Global.getLoginStatus = intent.getStringExtra("loginStatus");
                Log.d(TAG,"getTask: "+Global.getTask);
                displayReceiver();
            }catch (NullPointerException ex){
                Log.d(TAG,"Exception: "+ex);
            }
        }
    };

    public void displayReceiver(){
        try{
            if(Global.getLoginStatus.contains("LOGOUT")){
                instance.pushLogout();
            }
            if (Global.getTask.contains("0") || Global.getTask.isEmpty()){
                notifyTask.setVisibility(View.INVISIBLE);
            }else{
                notifyTask.setVisibility(View.VISIBLE);
                notifyTask.setText(Global.getTask);
            }
            if(Global.getQueue.contains("0") || Global.getQueue.isEmpty()){
                notifyQueue.setVisibility(View.INVISIBLE);
            }else {
                notifyQueue.setVisibility(View.VISIBLE);
                notifyQueue.setText(Global.getQueue);
            }
            if(Global.getMessage.equals("")){
                broadcastInfo.setVisibility(View.INVISIBLE);
            }else {
                broadcastInfo.setVisibility(View.VISIBLE);
                broadcastInfo.setText(Global.getMessage);
            }
        }catch (NullPointerException np){
            Log.e(TAG,"NullPointer"+np);
        }
    }

    public void pushLogout() {
        deviceService.stopSwift();
        deviceService.logOut();
    }

    public void MyTask(View v){
       try {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("my.com.tm.swift","my.com.tmrnd.swift.TTDetail.MainTT"));
            startActivity(intent);
        }catch (Exception ex){
            Log.d("StartActivityException", ex.toString());
        }
        clicked=true;
    }

    public void Queue(View v){
        try {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("my.com.tm.swift","my.com.tmrnd.swift.QueueDisplay.QueueList"));
            startActivity(intent);
        }catch (Exception ex){
            Log.d("StartActivityException", ex.toString());
        }
        click=true;
    }

    public void testConn(View v){
        deviceInfo.testNetwork();
    }

    public void logOut(View v){
        try {
            alertDialog = new AlertDialog.Builder(HomeScreen.this);
            alertDialog.setIcon(R.drawable.ic_power_white);
            alertDialog.setTitle("Sign Out...");
            alertDialog.setMessage("Do you want to sign out?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                    deviceService.stopSwift();
                    deviceService.logOut();
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }catch (Exception ex){
            Log.d("SwiftExeception",ex.toString());
        }
    }

    @Override
    public void onBackPressed() {
        //nothing
    }
}