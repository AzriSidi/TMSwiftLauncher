package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.plamera.tmswiftlauncher.Encap.UserDetail;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeScreen extends FragmentActivity {
    public static DatabaseHandler db;
    String DisplayUsername,carrierName;
    Boolean checkServerRunning = false;
    Boolean CheckNetworkRunning = false;
    Boolean clicked = false;
    Boolean click = false;
    ProgressDialog pd;
    @SuppressLint("StaticFieldLeak")
    public static TextView myScroller,notifyTask,notifyQueue,
            networkProvider,signalInfo,broadcastInfo,swiftVer,
            agentVer,serverName,appVer;
    String Serveradd,ServerName;
    BroadcastReceiver networkStateReceiver;
    Timer CheckNetworkTimer;
    TelephonyManager tel;
    static AlertDialog.Builder alertDialog;
    Intent intent;
    String TAG = "HomeScreen";
    private Boolean InitTaskRunning = false;
    public boolean logininvisible = false;
    private static ProgressDialog pdinit;
    String serverDateStr = "";
    Boolean dateMismatch = false;
    Boolean mismatchDialogDisplayed = false;
    String SimState;
    String username;
    static HomeScreen instance;
    DeviceOperate device;
    int timeout = 300000;
    IntentFilter iFilter,networkIntentFilter;
    String urlSwift = "http://10.54.97.227:8888/";
    DeviceService deviceService;
    DeviceInfo deviceInfo;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.home_screen);

        myScroller = findViewById(R.id.textView1);
        networkProvider = findViewById(R.id.textView2);
        broadcastInfo = findViewById(R.id.textView4);
        signalInfo = findViewById(R.id.textView7);
        swiftVer = findViewById(R.id.textView17);
        agentVer = findViewById(R.id.textView18);
        serverName = findViewById(R.id.textView19);
        appVer = findViewById(R.id.textView20);
        notifyTask = findViewById(R.id.badge_notification);
        notifyQueue = findViewById(R.id.badge_notification_1);
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        DisplayUsername = username;
        db = new DatabaseHandler(this);
        device = new DeviceOperate(this);
        deviceService = new DeviceService(this);
        deviceInfo = new DeviceInfo(this);

        registerReceiver();
        getSwiftApp();
        IntentData();
        deviceService.startTrackLog();
        if(deviceService.isMyServiceRunning()){
            Log.e(TAG, "SwiftState: Already running");
        }else {
            deviceService.startSwift();
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

            CheckNetworkTimer = new Timer();
            CheckNetworkTimer.schedule(new CheckNetworkTimerMethod(), 0, 5000);

            registerReceiver();
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
            if (Global.FirstTimeRunLogin) {
                if (!InitTaskRunning) {
                    Log.d("Login",
                            "FirstTimeRunLogin true & InitTaskRunning false");
                    pdinit = ProgressDialog.show(HomeScreen.this, "",
                            "Please wait for system initialization");
                    (new InitTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

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
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Login", "Error onstart " + e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkStateReceiver);
        CheckNetworkTimer.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        device.unregisterReceiver(this);
        unregisterReceiver(MyReceiver);
    }

    public void IntentData(){
        intent = getIntent();
        username = intent.getStringExtra("username");
        Log.d(TAG, "Global.username=" + username);
        Global.usernameBB = intent.getStringExtra("dataStaff");
        Log.d(TAG, "Global.usernameBB=" + Global.usernameBB);
        Global.passwordBB = intent.getStringExtra("password");
        Log.d(TAG, "Global.passwordBB=" + Global.passwordBB);
        Global.loginServer = intent.getStringExtra("loginServer");
        Log.d(TAG, "Global.loginServer=" + Global.loginServer);
        Global.UserType = intent.getStringExtra("loginType");
        Log.d(TAG, "Global.UserType=" + Global.UserType);
        Global.IMEIPhone = intent.getStringExtra("imei");
        Log.d(TAG, "Global.IMEIPhone=" + Global.IMEIPhone);
        Global.IMSIsimCardPhone = intent.getStringExtra("imsi");
        Log.d(TAG, "Global.IMSIPhone=" + Global.IMSIsimCardPhone);
        Global.frmVersion = intent.getStringExtra("firm_ver");
        Log.d(TAG, "Global.frmVersion=" + Global.frmVersion);
        Global.strVersion = intent.getStringExtra("strVersion");
        Log.d(TAG, "Global.strVersion=" + Global.strVersion);
        DisplayUsername = username;
        Log.d(TAG, "DisplayUsername: " + DisplayUsername);
    }

    public void deviceState(){
        SimState = deviceInfo.getSimState();
        carrierName = deviceInfo.getCarrier();
        networkProvider.setText(carrierName+" | "+deviceInfo.getLocalIP()+" | "+SimState);
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

    public void registerReceiver(){
        iFilter = new IntentFilter("com.plamera.CUSTOM_INTENT");
        registerReceiver(MyReceiver, iFilter);
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
        if (checkServerRunning) {
            pd = ProgressDialog.show(this, "",
                    "Please Wait... Testing Server Connection",
                    true, false);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    pd.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HomeScreen.this, "Server Status: Testing fail",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, timeout);
            CheckServerStatus myCheckServerStatus = new CheckServerStatus();
            myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (!checkServerRunning) {
            checkServerRunning = true;
            if (Global.connectedToWiFi) {
                CheckServerStatus myCheckServerStatus = new CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (Global.connected3G) {
                CheckServerStatus myCheckServerStatus = new CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            try {
                Toast.makeText(this, "IP: " + deviceInfo.getLocalIP(),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private class InitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // disable login button here
            Log.d("InitTask", "Start");
            InitTaskRunning = true;
            if (Global.LogAsAdmin) {
                Global.LogAsAdmin = false;
            }
            deviceInfo.queryNetwork();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Global.ServerStatus = "Connected";
            // myScroller
            // .setBackgroundColor(Color
            // .parseColor("#210B61"));
            Log.d("InitTask", "doInBackground");

            if (!DisplayUsername.contains("*") && Global.connected3G) {
                Log.d("InitTask", "FirstTimeRunLogin");

                if (Global.FirstTimeRunLogin) {
                    Global.FirstTimeRunLogin = false;
                    Log.d("InitTask", "Checking server date/time "
                            + Global.usernameBB + ":");

                    SimpleDateFormat sdfDate = new SimpleDateFormat(
                            "MM/dd/yyyy hh:mm:ss a");
                    Date currentDate = new Date();
                    Date serverDate;
                    serverDateStr = "";
                    dateMismatch = false;

                    try {
                        HttpParams httpParameters = new BasicHttpParams();
                        // set timeout to 5 minute
                        HttpConnectionParams.setConnectionTimeout(
                                httpParameters, timeout);
                        HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                        HttpClient client = new DefaultHttpClient(
                                httpParameters);

                        // if (Global.connected3G) {
                        //
                        // Global.URLSwift = "http://10.41.102.70/";
                        // Serveradd = Global.URLSwift
                        // + "Mobile/Configuration/time.php";
                        //
                        // } else {
                        if (DisplayUsername.contentEquals("TM")) {
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        } else if (DisplayUsername.contains("*")) {
                            // Global.URLSwift = "http://58.26.233.1:8080/";
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                            // Global.URLSwift =
                            // "http://swift.tmrnd.com.my:8080/";
                        } else if (DisplayUsername.contains("#")) {
                            Global.URLSwift = "http://10.44.11.64:8090/";
                        } else if (DisplayUsername.contains("$")) {
                            Global.URLSwift = "http://10.106.132.7/";
                        } else if (DisplayUsername.contains("@")) {
                            Global.URLSwift = "http://10.41.102.81/";
                        } else if (DisplayUsername.contains("!")) {
                            Global.URLSwift = "http://10.41.102.70/";
                        } else {
                            Global.URLSwift = "http://10.41.102.70/";
                        }

                        // Global.URLSwift = "http://10.41.102.70/"; // hisham
                        // add
                        // cause
                        // "http://swift.tmrnd.com.my:8080/"
                        // always
                        // fail
                        Serveradd = Global.URLSwift
                                + "Mobile/Configuration/time.php";

                        if (DisplayUsername.contains("@")
                                || DisplayUsername.contains("$")) {
                            Serveradd = Global.URLSwift
                                    + "preprod/Mobile/Configuration/time.php";

                        }
                        // }

                        Log.d("InitTask", "Checking server date/time "
                                + Global.URLSwift);
                        HttpGet request = new HttpGet(Serveradd); // HttpGet
                        // request
                        // = new
                        // HttpGet(
                        // "http://10.41.102.70/Mobile/Configuration/time.php");
                        HttpResponse response = client.execute(request);

                        InputStream in = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in));
                        StringBuilder str = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            str.append(line);
                        }
                        in.close();
                        serverDateStr = str.toString().trim();
                        serverDate = sdfDate.parse(serverDateStr);
                        if (serverDateStr.length() == 22) {
                            Global.ServerStatus = "Connected";
                            // Log.d("Login", " Check Date check OK" + Serveradd
                            // + DisplayUsername);
                        } else {
                            Global.ServerStatus = "Not Connected";
                            // Log.d("Login", " Check Date Not OK" + Serveradd
                            // + DisplayUsername);
                        }

                        long diff = Math.abs(serverDate.getTime()
                                - currentDate.getTime());
                        // check if times are within 10 minutes
                        if (diff > 600000) {
                            // Log.d("InitTask",
                            // "Date mismatch, server: "
                            // + sdfDate.format(serverDate)
                            // + ", device: "
                            // + sdfDate.format(currentDate));
                            // put date in a simpler format
                            SimpleDateFormat sdfDateHuman = new SimpleDateFormat(
                                    "EEE, d MMM yyyy, h:mm:ss a");
                            serverDateStr = sdfDateHuman.format(serverDate);
                            dateMismatch = true;
                        } else {
                            // Log.d("InitTask",
                            // "Date OK, server: "
                            // + sdfDate.format(serverDate)
                            // + ", device: "
                            // + sdfDate.format(currentDate));
                            dateMismatch = false;
                        }

                    } catch (Exception e) {
                        Log.e("InitTask", e.toString());
                        Global.ServerStatus = "Not Connected";
                        Log.e("InitTask", "Not Connected" + Serveradd
                                + DisplayUsername);

                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            InitTaskRunning = false;
            // ahmad to prevent crash
            if (pdinit != null) {
                if (pdinit.isShowing()) {
                    pdinit.dismiss();
                }
            }

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
            myScroller.setText(Global.myStatus + " | SERVER: "
                    + Global.ServerStatus);
            // re-enable login button here
            // loginButton.setEnabled(true);
            if (dateMismatch) {
                if (!mismatchDialogDisplayed) {
                    mismatchDialogDisplayed = true;
                    String MyMessage = "Please check and correct the date & time in your device\nServer date & time: "
                            + serverDateStr;
                    new AlertDialog.Builder(HomeScreen.this)
                            .setTitle("Incorrect device date/time")
                            .setMessage(MyMessage)
                            .setCancelable(false)
                            .setNeutralButton("OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            mismatchDialogDisplayed = false;
                                            startActivity(new Intent(
                                                    android.provider.Settings.ACTION_DATE_SETTINGS));
                                        }

                                    }).show();
                } else {

                }
                // not disturb user again and again
                dateMismatch = false;
            }

            Log.d("InitTask", "Exit");
            // if (pdinit.isShowing()) {
            // pdinit.dismiss();
            // }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : CheckServerStatus
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public class CheckServerStatus extends AsyncTask<Void, Void, Void> {

        String ServerStatus;
        String ServerTime;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                if (Global.connected3G || Global.connectedToWiFi
                        && Global.loginServer != "SIT") {

                    if ( Global.loginServer == "SIT") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        // Global.URLSwift = "http://58.26.233.1:8080/";
                    } else if (Global.loginServer == "DEV") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.44.11.64:8090/";
                    } else if ( Global.loginServer == "PRE OLD") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.106.132.7/";
                        // Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                    } else if (Global.loginServer == "PRE") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.81/";
                    } else {
                        Global.URLSwift = urlSwift;
                        //Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.70/";
                    }

                    Serveradd = Global.URLSwift + "serverInfo.php";

                    HttpParams httpParameters = new BasicHttpParams();
                    // set timeout to 5 minute
                    HttpConnectionParams.setConnectionTimeout(httpParameters,
                            timeout);
                    HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                    HttpClient client = new DefaultHttpClient(httpParameters);

                    HttpGet request = new HttpGet(Serveradd);

                    HttpResponse response = client.execute(request);

                    InputStream in = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    StringBuilder str = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        str.append(line);
                    }
                    in.close();
                    String serverDateStr = str.toString().trim();
                    ServerStatusExtract(serverDateStr);

                    if (ServerStatus.equals("OK")) {

                        Global.ServerStatus = "Connected to " + ServerName;
                        Global.ServerDate = ServerTime;
                        Log.d(TAG,"CheckServerStatus:"+" HTTP check OK "
                                + Serveradd);
                    } else {
                        Global.ServerStatus = "Not Connected";
                        Log.d(TAG,"CheckServerStatus:"+" HTTP check Not OK "
                                + Serveradd + DisplayUsername);
                        if (Global.currentAPN.contains("Maxis")
                                && Global.connected3G) {
                            HomeScreen.PingServerStatus myPingServerStatus = new HomeScreen.PingServerStatus();
                            myPingServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                    Log.d("CheckQueryNetworkTry",Global.ServerStatus);
                    if (Global.loginContext != null) {
                        ((Activity) Global.loginContext)
                                .runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                       if (Global.ServerStatus
                                                .contains("Not Connected")) {

                                            myScroller
                                                    .setBackgroundColor(Color.RED);

                                        } else if (Global.ServerStatus
                                                .contains("Unknown")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);

                                        } else {
                                           if (Global.myStatus.contains("WIFI")) {

                                               myScroller
                                                       .setBackgroundColor(Color

                                                               .parseColor("#FF8000"));

                                           } else if (Global.connected3G) {
                                               myScroller
                                                       .setBackgroundColor(Color

                                                               .parseColor("#210B61"));

                                           }

                                       }
                                        myScroller.setText(Global.myStatus
                                                + " |  SERVER: "
                                                + Global.ServerStatus);
                                    }

                                });
                    }
                } else {
                    // Log.e("Login","SIT");
                    if (DisplayUsername.contains("*") && Global.connectedToWiFi) {
                        Global.ServerStatus = "Connected to gponems";

                        if (Global.loginContext != null) {
                            ((Activity) Global.loginContext)
                                    .runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#FF8000"));
                                            myScroller.setText(Global.myStatus
                                                    + " |  SERVER: "
                                                    + Global.ServerStatus);
                                        }

                                    });
                        }
                    }

                }

            } catch (Exception e) {
                ServerTime = "";
                Log.e("Login CheckServerStatus", " HTTP check Not OK "
                        + Serveradd);

                Global.ServerStatus = "Not Connected";
                if (Global.currentAPN.contains("Maxis") && Global.connected3G) {
                    HomeScreen.PingServerStatus myPingServerStatus = new HomeScreen.PingServerStatus();
                    myPingServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
                }
                if (Global.loginContext != null) {
                    ((Activity) Global.loginContext)
                            .runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (Global.ServerStatus
                                            .contains("Not Connected")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);

                                    } else if (Global.ServerStatus
                                            .contains("Unknown")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);

                                    } else {
                                        if (Global.myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#FF8000"));

                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#210B61"));

                                        }

                                    }
                                    myScroller.setText(Global.myStatus
                                            + " |  SERVER: "
                                            + Global.ServerStatus);
                                }

                            });
                    Log.d("CheckQueryNetworkCatch",Global.ServerStatus);
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG, "Server Status: " + Global.ServerStatus);
            CheckNetworkRunning = false;
            if (Global.ServerStatus.contains("Connected to")){
                if (pd != null) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                        timer.cancel();
                    }
                }
                if (checkServerRunning) {
                    checkServerRunning = false;
                    Toast.makeText(HomeScreen.this, "Server Status: " + Global.ServerStatus,
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }

        private void ServerStatusExtract(String strData) {
            try {
                // Log.i("Pian","ServerData="+strData);
                int start = 0;
                int end = 0;
                ServerName = "";
                start = strData.indexOf("<serverName>");
                end = strData.indexOf("</serverName>");
                ServerName = strData.substring(start + 12, end - 15);
                // Log.i("Pian","ServerName="+ServerName);
                start = strData.indexOf("<serverStatus>");
                end = strData.indexOf("</serverStatus>");
                ServerStatus = strData.substring(start + 14, end);
                // Log.i("Pian","ServerStatus="+ServerStatus);
                start = strData.indexOf("<serverTime>");
                end = strData.indexOf("</serverTime>");
                ServerTime = strData.substring(start + 12, end);
                // Log.i("Pian","ServerTime="+ServerTime);
                // start-07/05/2015 @Pian - tarikh dari server untuk req/update
                // TT dan FF
                try {
                    Global.ServerDate = ServerTime;
                    String splitDT[] = Global.ServerDate.split(" ");
                    String strDt = splitDT[0];
                    String strTm = splitDT[1];
                    Log.i("Pian", "Date=" + strDt);
                    Log.i("Pian", "Time=" + strTm);
                    SimpleDateFormat inFormat = new SimpleDateFormat(
                            "yyyy-MM-dd");
                    SimpleDateFormat outFormat = new SimpleDateFormat(
                            "MM/dd/yyyy");
                    Date dt = inFormat.parse(strDt);
                    Global.reqUsingServerDate = outFormat.format(dt).toString();
                    Global.updateUsingServerDateTime = Global.reqUsingServerDate
                            + " " + strTm;
                    Log.i("Pian", "Global.reqUsingServerDt="
                            + Global.reqUsingServerDate);
                    Log.i("Pian", "Global.updateUsingServerDateTime="
                            + Global.updateUsingServerDateTime);
                } catch (Exception e) {
                    Log.i("Pian", e.toString());
                }
                // end-07/05/2015 @Pian - tarikh dari server untuk req/update TT
                // dan FF
            } catch (Exception e) {

            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : CheckNetworkTimerMethod
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    class CheckNetworkTimerMethod extends TimerTask {
        public void run() {
            deviceInfo.getLocalIP();
            if (!CheckNetworkRunning) {
                CheckNetworkRunning = true;
                if (Global.connectedToWiFi) {
                    CheckServerStatus myCheckServerStatus = new HomeScreen.CheckServerStatus();
                    myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (Global.connected3G) {
                    CheckServerStatus myCheckServerStatus = new HomeScreen.CheckServerStatus();
                    myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : PingServerStatus
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ping to maxis gateway
    public class PingServerStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                if (Global.connected3G) {
                    String pingCmd = "ping -c 1 -w 25 -s 1 " + deviceInfo.getLocalIP();
                    java.lang.Process p1 = java.lang.Runtime.getRuntime().exec(pingCmd);

                    int returnVal = p1.waitFor();
                    // boolean reachable = (returnVal == 0);

                    if (returnVal == 0) {
                        Global.CanPing = true;

                        Log.d("Login PingServerStatus", " ping check OK");
                    } else {
                        Global.CanPing = false;
                        Global.ServerStatus = "Not Connected";
                        Global.myStatus = "Network Status: Ping Fail ";

                        Log.d("Login PingServerStatus", " ping check Not OK "
                                + Global.MaxisRouter);
                    }
                    if (Global.loginContext != null) {
                        ((Activity) Global.loginContext)
                                .runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (Global.ServerStatus
                                                .contains("Not Connected")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else if (Global.ServerStatus
                                                .contains("Unknown")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else {
                                            if (Global.myStatus.contains("WIFI")) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#FF8000"));
                                            } else if (Global.connected3G) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#210B61"));
                                            }

                                        }

                                        myScroller.setText(Global.myStatus
                                                + " |  SERVER: "
                                                + Global.ServerStatus);
                                    }

                                });
                    }
                }

            } catch (Exception e) {
                Log.e("Login PingServerStatus",
                        " Ping check error" + e.toString());
                Global.CanPing = false;
                Global.ServerStatus = "Not Connected";
                Global.myStatus = "Network Status: Ping Fail ";

                Log.d("Login PingServerStatus", " ping check Not OK "
                        + Global.MaxisRouter);
                if (Global.loginContext != null) {
                    ((Activity) Global.loginContext)
                            .runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (Global.ServerStatus
                                            .contains("Not Connected")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    } else if (Global.ServerStatus
                                            .contains("Unknown")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    } else {
                                        if (Global.myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#FF8000"));
                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#210B61"));
                                        }

                                    }

                                    if (!Global.CanPing) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    }
                                    myScroller.setText(Global.myStatus
                                            + " |  SERVER: "
                                            + Global.ServerStatus);
                                }

                            });
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (Global.CanPing) {
                deviceInfo.queryNetwork();
            } else {
                CheckNetworkRunning = false;
                if (checkServerRunning) {
                    checkServerRunning = false;
                    if (pd != null) {
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                    Toast.makeText(HomeScreen.this, "Network Status: Ping Fail",
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }
    }
}