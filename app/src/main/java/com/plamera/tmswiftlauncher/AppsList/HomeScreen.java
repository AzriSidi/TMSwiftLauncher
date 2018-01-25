package com.plamera.tmswiftlauncher.AppsList;

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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.plamera.tmswiftlauncher.DatabaseHandler;
import com.plamera.tmswiftlauncher.DeviceOperate;
import com.plamera.tmswiftlauncher.Global;
import com.plamera.tmswiftlauncher.LauncherService;
import com.plamera.tmswiftlauncher.MainActivity;
import com.plamera.tmswiftlauncher.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
    private String myStatus = "";
    @SuppressLint("StaticFieldLeak")
    public static TextView myScroller,notifyTask,notifyQueue,
            networkProvider,signalInfo,broadcastInfo,appDetail,
            phoneInfo,userName,swiftVer,agentVer,serverName,ldapStatus;
    String Serveradd,ServerName;
    BroadcastReceiver networkStateReceiver;
    Timer CheckNetworkTimer;
    TelephonyManager tel;
    MyPhoneStateListener MyListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.home_screen);
        db = new DatabaseHandler(this);

        ImageView image = findViewById(R.id.imageView);
        image.setImageResource(R.drawable.tm_white);
        userName = findViewById(R.id.textView);
        myScroller = findViewById(R.id.textView1);
        networkProvider = findViewById(R.id.textView2);
        signalInfo = findViewById(R.id.textView3);
        broadcastInfo = findViewById(R.id.textView4);
        signalInfo = findViewById(R.id.textView7);
        phoneInfo = findViewById(R.id.textView8);
        appDetail = findViewById(R.id.textView16);
        swiftVer = findViewById(R.id.textView17);
        agentVer = findViewById(R.id.textView18);
        serverName = findViewById(R.id.textView19);
        ldapStatus = findViewById(R.id.textView20);

        notifyTask = findViewById(R.id.badge_notification);
        notifyQueue = findViewById(R.id.badge_notification_1);
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        DisplayUsername = username;
        device = new DeviceOperate(this);
        getSwiftApp();
        IntentData();
        deviceService();
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            broadcastSend();
            CheckNetworkRunning = false;
            Global.CreateMainMenu = false;
            Global.TTreqSummDate = "";
            Global.FFreqSummDate = "";
            networkStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    CheckNetworkRunning = false;
                    checkServerRunning = false;
                    Global.CanPing = true;
                    queryNetwork();
                    deviceState();
                }
            };
            IntentFilter networkIntentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateReceiver, networkIntentFilter);
            CheckNetworkTimer = new Timer();
            CheckNetworkTimer.schedule(new CheckNetworkTimerMethod(), 0, 5000);
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

        /*if(clicked){
            notifyTask.setVisibility(View.INVISIBLE);
        }if(click) {
            notifyQueue.setVisibility(View.INVISIBLE);
        }*/
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
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Login", "Error onstart " + e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(networkStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CheckNetworkTimer.cancel();
    }

    @Override
    public void onDestroy(){
        device.unregisterReceiver(this);
        super.onDestroy();
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
        int simState = tel.getSimState();
        carrierName = tel.getNetworkOperatorName();
        switch (simState) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                SimState = "UNKNOWN";
                break;
        }
        if(SimState == null){
            MyListener = new HomeScreen.MyPhoneStateListener();
            tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    | PhoneStateListener.LISTEN_SERVICE_STATE);
        }else {
            signalInfo.setText(" | Not available");
            phoneInfo.setText(" | Not available");
        }

        Log.d("getLocalIP: ",getLocalIP());
        if(carrierName.equals("")){
            networkProvider.setText("Not available"+" | "+getLocalIP());
        }else {
            networkProvider.setText(carrierName+" | "+getLocalIP());
        }
    }

    private void getSwiftApp() {
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo("my.com.tm.swift",0);
            String versionName = app.versionName;
            String loginState = "";
            appDetail.setText("SWIFT - "+versionName + "  |  " );
            swiftVer.setText("LAUNCHER - "+Global.launcherVer + "  |  ");
            agentVer.setText("EMM - "+Global.agentVer+ "  |  ");
            serverName.setText(Global.loginServer+ "  |  ");
            if(Global.ldapStatus.contains("true")){
                loginState = "LDAP";
            }else {
                loginState = "LOCAL";
            }
            ldapStatus.setText(loginState);
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void broadcastSend(){
        try {
            intent.setComponent(new ComponentName("my.com.tm.swift", "my.com.tmrnd.swift.LocationUpdateService"));
            Log.d(TAG, "Starting Check TT Receiver");
            intent.putExtra("staffID", Global.usernameBB);
            intent.putExtra("password", Global.passwordBB);
            intent.putExtra("imei", Global.IMEIPhone);
            intent.putExtra("imsi", Global.IMSIsimCardPhone);
            intent.putExtra("firmVer", Global.frmVersion);
            intent.putExtra("serverStatus", Global.loginServer);
            intent.putExtra("loginType", Global.UserType);
            intent.putExtra("token", Global.getToken);
            startService(intent);
        }catch (Exception ex) {
            Log.d(TAG,"BroadcastExeception: "+ex.toString());
        }
    }

    public void deviceService(){
        try {
            intent = new Intent(this, LauncherService.class);
            startService(intent);
        }catch (Exception ex){
            Log.d(TAG,"broadcastExeception: "+ex.toString());
        }
    }

    public static class MyReceiver extends BroadcastReceiver {
        String TAG = "MyReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String getMessage = intent.getStringExtra("dataMessage");
                String getTask = intent.getStringExtra("dataTask");
                String getQueue = intent.getStringExtra("dataQueue");
                Global.getLoginStatus = intent.getStringExtra("loginStatus");
                Log.d(TAG,"getTask: "+getTask);
                Log.d(TAG,"getMessage: "+getMessage);
                Log.d(TAG,"getLoginStatus: "+Global.getLoginStatus);
                if(Global.getLoginStatus.equals("LOGOUT")){
                    instance.pushLogout();
                }
                if (getTask.equals("0")){
                    notifyTask.setVisibility(View.INVISIBLE);
                }else{
                    notifyTask.setVisibility(View.VISIBLE);
                    notifyTask.setText(getTask);
                }
                if(getQueue.equals("0")){
                    notifyQueue.setVisibility(View.INVISIBLE);
                }else {
                    notifyQueue.setVisibility(View.VISIBLE);
                    notifyQueue.setText(getQueue);
                }
                if(getMessage.equals("")){
                    broadcastInfo.setVisibility(View.INVISIBLE);
                }else {
                    broadcastInfo.setVisibility(View.VISIBLE);
                    broadcastInfo.setText(getMessage);
                }
            }catch (NullPointerException ex){
                Log.d(TAG,"Exception: "+ex);
            }
        }
    }

    public void pushLogout() {
        intent = new Intent();
        intent.setComponent(new ComponentName("my.com.tm.swift", "my.com.tmrnd.swift.LocationUpdateService"));
        stopService(intent);
        intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        Global.loginResult = false;
        Global.getToken = "";
        Global.status = "Offline";
        db.deleteContact();
        finish();
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        int dbmLevel,asuLevel;
        String signal,phoneState;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            asuLevel = signalStrength.getGsmSignalStrength();
            dbmLevel = (signalStrength.getGsmSignalStrength() * 2) - 113;
            signal = dbmLevel + " dBm"+ " " +asuLevel+" asu";
            Log.d("SignalStrengthApp",signal);
                signalInfo.setText(" | "+signal);
        }

        @Override
        public void onServiceStateChanged (ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            int state = serviceState.getState();
            Log.d("Phone State", String.valueOf(state));
            switch(state) {
                case ServiceState.STATE_EMERGENCY_ONLY:
                    phoneState ="Emergency Only";
                    break;
                case ServiceState.STATE_IN_SERVICE:
                    phoneState ="In Service";
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                    phoneState ="Out Of Service";
                    break;
                case ServiceState.STATE_POWER_OFF:
                    phoneState ="POWER_OFF";
                    break;
                default:
                    phoneState = "Unknown";
                    break;
            }
            Log.d("Phone State",phoneState);
            phoneInfo.setText(" | "+phoneState);
        }
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
        try {
            Toast.makeText(HomeScreen.this, "IP: " + getLocalIP(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (checkServerRunning) {
            pd = ProgressDialog
                    .show(HomeScreen.this, "",
                            "Please Wait... Testing Server Connection",
                            true, false);
            long delayInMillis = 1000;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    pd.dismiss();
                }
            }, delayInMillis);
        } else if (!checkServerRunning) {
            checkServerRunning = true;
            if (Global.connectedToWiFi) {
                HomeScreen.CheckServerStatus myCheckServerStatus = new HomeScreen.CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (Global.connected3G) {
                HomeScreen.CheckServerStatus myCheckServerStatus = new HomeScreen.CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public void logOut(View v){
        try {
            //alert_box
            alertDialog = new AlertDialog.Builder(HomeScreen.this);
            alertDialog.setIcon(R.drawable.ic_power_white);
            alertDialog.setTitle("Sign Out...");
            alertDialog.setMessage("Do you want to sign out?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                intent = new Intent();
                intent.setComponent(new ComponentName("my.com.tm.swift",
                        "my.com.tmrnd.swift.LocationUpdateService"));
                stopService(intent);
                intent = new Intent(HomeScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                Global.loginResult = false;
                Global.getToken = "";
                Global.status = "Offline";
                db.deleteContact();
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //nothing
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
            queryNetwork();
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
                if (myStatus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
            myScroller.setText(myStatus + " | Server: "
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
                        Global.URLSwift = "http://10.54.97.227:8888/";
                        //Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.70/";
                    }

                /*
                if (Global.connected3G || Global.connectedToWiFi
                        && !DisplayUsername.contains("*")) {
                    // if (Global.connected3G) {
                    // Global.URLSwift = "http://10.41.102.70/";
                    // } else {
                    if (DisplayUsername.contentEquals("TM")) {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                    } else if (DisplayUsername.contains("*")) {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        // Global.URLSwift = "http://58.26.233.1:8080/";
                    } else if (DisplayUsername.contains("#")) {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.44.11.64:8090/";
                    } else if (DisplayUsername.contains("$")) {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.106.132.7/";
                        // Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                    } else if (DisplayUsername.contains("@")) {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.81/";
                    } else {
                        //Global.URLSwift = "http://10.54.7.214/";
                        Global.URLSwift = "http://10.54.97.227:8888/";
                        //Global.URLSwift = "http://10.41.102.70/";
                    }*/

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
                                           if (myStatus.contains("WIFI")) {

                                               myScroller
                                                       .setBackgroundColor(Color

                                                               .parseColor("#FF8000"));

                                           } else if (Global.connected3G) {
                                               myScroller
                                                       .setBackgroundColor(Color

                                                               .parseColor("#210B61"));

                                           }

                                       }
                                        myScroller.setText(myStatus
                                                + " |  Server: "
                                                + Global.ServerStatus);
                                        Log.d("myStatus4: ",myStatus);
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
                                            myScroller.setText(myStatus
                                                    + " |  Server: "
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
                                        if (myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#FF8000"));

                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#210B61"));

                                        }

                                    }
                                    myScroller.setText(myStatus
                                            + " |  Server: "
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
            CheckNetworkRunning = false;
            if (checkServerRunning) {
                checkServerRunning = false;
                if (pd != null) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                }
                Toast.makeText(HomeScreen.this,
                        "Server Status: " + Global.ServerStatus,
                        Toast.LENGTH_SHORT).show();
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
            getLocalIP();

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
    // FUNCTION NAME : queryNetwork
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    private void queryNetwork() {
        String usernameBB = Global.usernameBB;
        String netPre = "";
        try {
            myStatus = "\u00A0\u00A0\u00A0\u00A0"+usernameBB+"  |  ";
            ConnectivityManager connMgr = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            Global.connected3G = mobile.isConnected();
            Global.connectedToWiFi = wifi.isConnected();
            // added by amir on 2013-01-08

            if (Global.connected3G || Global.connectedToWiFi) {
                Global.localIP = getLocalIP();
            }

            String activeConn;
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
            } else if (Global.connected3G) {
                // Global.URLSwift = "http://10.41.102.70/";
                activeConnPlus += "";
                Global.netType = netPre+"/"+activeConnPlus;
            } else if ((!Global.connectedToWiFi) && (!Global.connected3G)) {
                Global.localIP = getLocalIP();
                activeConnPlus = "None";
                Global.ServerStatus = "Not Connected";
                Global.netType = activeConnPlus;
            }

            myStatus += activeConnPlus;
            myScroller.setText(myStatus + " |  Server: "
                    + Global.ServerStatus);

            if (Global.ServerStatus.contains("Not Connected")) {
                myScroller.setBackgroundColor(Color.RED);
            } else if (Global.ServerStatus.contains("Unknown")) {
                myScroller.setBackgroundColor(Color.RED);
            } else {
                if (activeConnPlus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: "+e.toString());
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : getLocalIP
    // //////////////////////////////////////////////////////////////////////////////////////////////////
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
        return "None";
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
                    String pingCmd = "ping -c 1 -w 25 -s 1 " + getLocalIP();
                    java.lang.Process p1 = java.lang.Runtime.getRuntime().exec(pingCmd);

                    int returnVal = p1.waitFor();
                    // boolean reachable = (returnVal == 0);

                    if (returnVal == 0) {
                        Global.CanPing = true;

                        Log.d("Login PingServerStatus", " ping check OK");
                    } else {
                        Global.CanPing = false;
                        Global.ServerStatus = "Not Connected";
                        myStatus = "Network Status: Ping Fail ";

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
                                            if (myStatus.contains("WIFI")) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#FF8000"));
                                            } else if (Global.connected3G) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#210B61"));
                                            }

                                        }

                                        myScroller.setText(myStatus + myStatus
                                                + " |  Server: "
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
                myStatus = "Network Status: Ping Fail ";

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
                                        if (myStatus.contains("WIFI")) {
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
                                    myScroller.setText(myStatus + myStatus
                                            + " |  Server: "
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
                queryNetwork();
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